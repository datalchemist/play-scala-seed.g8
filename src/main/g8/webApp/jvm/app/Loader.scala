
import com.lightbend.lagom.scaladsl.api.{LagomConfigComponent,ServiceAcl, ServiceInfo}
import com.lightbend.lagom.scaladsl.client.LagomServiceClientComponents
import com.lightbend.lagom.scaladsl.client.ConfigurationServiceLocatorComponents
import com.lightbend.lagom.scaladsl.devmode.LagomDevModeComponents

import play.api.{ApplicationLoader, BuiltInComponentsFromContext, Mode}
import play.api.ApplicationLoader.Context
import play.api.i18n.I18nComponents
import play.api.libs.ws.ahc.AhcWSComponents
import play.api.mvc.{ControllerComponents}
import play.filters.HttpFiltersComponents
import play.filters.https.{RedirectHttpsConfiguration,RedirectHttpsFilter}

import play.api.routing.{SimpleRouter,Router => PlayRouter}

import com.softwaremill.macwire._

import scala.collection.immutable
import scala.concurrent.ExecutionContext
import scala.concurrent.Future


import controllers.{PublicEndpoints,AssetsComponents,CountController,Application}
import router.{Routes => BaseRoutes}

abstract class WebApp(context: Context) extends BuiltInComponentsFromContext(context)
  with I18nComponents
  with AhcWSComponents
  with LagomServiceClientComponents
  with LagomConfigComponent
  with AssetsComponents 
  with HttpFiltersComponents{
  
  override lazy val serviceInfo: ServiceInfo = ServiceInfo(
    "web-app-server",
    Map(
      "web-app-server" -> immutable.Seq(ServiceAcl.forPathRegex("(/web|/assets|/api-endpoint).*"))
    )
  )
  override implicit lazy val executionContext: ExecutionContext = actorSystem.dispatcher
  lazy val ep:PublicEndpoints=  wire[PublicEndpoints]
  lazy val baseRouter : PlayRouter  = {
    val prefix = "/"
    wire[BaseRoutes]
  }
  override lazy val router =  {
    new SimpleRouter {
      def routes = 
        baseRouter.routes
        .orElse(
            ep.routes
        )
      override def documentation=baseRouter.documentation
    }
  }
  
  
  lazy val clock = java.time.Clock.systemDefaultZone
  
  //internal Play service
  lazy val counterService = wire[services.AtomicCounter]
  //ecternal Lagom service
  //lazy val simpleLagomService = serviceClient.implement[SimpleserviceService]
  
  lazy val main = wire[Application]
  lazy val countCtrl = wire[CountController]
}

class WebAppLoader extends ApplicationLoader {
  override def load(context: Context) =
    context.environment.mode match {
      case Mode.Dev =>
        new WebApp(context) with LagomDevModeComponents {}.application
      case _ =>
        new WebApp(context) 
        with ConfigurationServiceLocatorComponents {
          lazy val redirectFilter = new RedirectHttpsFilter(new RedirectHttpsConfiguration())
          override def httpFilters = {
            super.httpFilters :+ redirectFilter
          }
  //        override lazy val serviceLocator :ServiceLocator = NoServiceLocator
        }.application
    }
}
