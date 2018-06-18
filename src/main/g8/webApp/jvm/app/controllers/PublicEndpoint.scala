package controllers

import play.api.routing.{Router => PlayRouter}



import play.api.mvc.{DefaultActionBuilder, PlayBodyParsers, BodyParser}

class PublicEndpoints(counter: services.Counter,actionBuilder:DefaultActionBuilder, bodyParsers: PlayBodyParsers)(implicit ec:scala.concurrent.ExecutionContext) 
extends ApiEndpointServer(bodyParsers) {
  
  def defaultActionBuilder[A]=actionBuilder.async(_: BodyParser[A]) _
  
  val routes: PlayRouter.Routes = routesFromEndpoints(
    count.implementedBy(_ => counter.nextCount()) //,
    //LagomService(hello,lagomService.hello _)(defaultActionBuilder)
  )

}
