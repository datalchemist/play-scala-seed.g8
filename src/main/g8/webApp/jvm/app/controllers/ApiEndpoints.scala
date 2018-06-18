package controllers

import play.api.mvc.BodyParsers

import endpoints.algebra.JsonEntities
import endpoints.play.server.{ Endpoints}

import com.lightbend.lagom.scaladsl.api.{Service, ServiceCall}

import play.api.libs.json.{JsValue,Format, Json, Reads}
import play.api.mvc.{PlayBodyParsers,Result, BodyParser,Action, RequestHeader, Handler, Results, Request => PlayRequest}
import play.api.routing.{Router => PlayRouter}
import scala.concurrent.Future

/**
 * Play json JsonEntities implementation for endpoints
 */
trait PlayJsonEntities extends Endpoints with JsonEntities {
  val bodyParsers: PlayBodyParsers

  def jsonRequest[A : Format]: RequestEntity[A] = bodyParsers.json[A]
  def jsonResponse[A : Format]: Response[A] = a => Results.Ok(Json.toJson(a))

}
/**
 * Base Play json endpoint interpreter with some utils for using custom actionBuilder and for binding to Lagom services
 */
class ApiEndpointServer(val bodyParsers: PlayBodyParsers)(implicit ec:scala.concurrent.ExecutionContext) 
extends shared.PublicEndpoints with Endpoints with PlayJsonEntities {

  case class CustomActionEndpoint[A, B](endpoint: Endpoint[A, B], actionBuilder: BodyParser[A] => (PlayRequest[A] => Future[Result]) => Action[A], service : A => Future[B]) extends ToPlayHandler {
    def playHandler(header: RequestHeader): Option[Handler] = 
      endpoint.request.decode(header)
        .map { bodyParser =>
          actionBuilder(bodyParser) { ( request: PlayRequest[A]) =>
            service(request.body).map { b =>
              endpoint.response(b)
            }
          }
        }
  }
  implicit class RichEndpoint[A, B](ep:Endpoint[A, B]) {
    def implementedBy(service: A => Future[B], actionBuilder: BodyParser[A] => (PlayRequest[A] => Future[Result]) => Action[A]): CustomActionEndpoint[A, B] = 
      CustomActionEndpoint(ep, actionBuilder, service)
  }   
  def LagomService[A,B,AB,C](endpoint:Endpoint[AB,C],service:A => ServiceCall[B,C])( actionBuilder: BodyParser[AB] => (PlayRequest[AB] => Future[Result])  => Action[AB])(implicit tuplerAB: endpoints.Tupler.Aux[A, B, AB])=
    endpoint.implementedBy((ab:AB) => {val (a,b)=tuplerAB.unapply(ab);service(a).invoke(b)},actionBuilder)
  def LagomService[B,C](endpoint:Endpoint[B,C],service: ServiceCall[B,C])( actionBuilder: BodyParser[B] => (PlayRequest[B] => Future[Result])  => Action[B])=
    endpoint.implementedBy((b:B) => service.invoke(b),actionBuilder)
  def LagomService[B,C](endpoint:Endpoint[B,C],service: B => ServiceCall[akka.NotUsed,C])( actionBuilder: BodyParser[B] => (PlayRequest[B] => Future[Result])  => Action[B])=
    endpoint.implementedBy((b:B) => service(b).invoke,actionBuilder)
  
}