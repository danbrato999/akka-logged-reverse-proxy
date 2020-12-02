package com.github.danbrato999

import akka.actor.typed.{ActorRef, ActorSystem}
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import com.github.danbrato999.api.{UserRegistry, UserRoutes}
import com.typesafe.config.ConfigFactory

import scala.util.{Failure, Success}

//#main-class
object QuickstartApp {
  //#start-http-server
  private def startReverseProxy(logger: ActorRef[ProxyMessage])(implicit system: ActorSystem[_]): Unit = {
    // Akka HTTP still needs a classic ActorSystem to start
    import system.executionContext

    val config = system.settings.config
    val handler = ProxyHandler(logger, config.getConfig("target"))
    val futureBinding = Http().newServerAt("localhost", config.getInt("port")).bind(handler.handle)
    futureBinding.onComplete {
      case Success(binding) =>
        val address = binding.localAddress
        system.log.info("Reverse proxy online at http://{}:{}/", address.getHostString, address.getPort)
      case Failure(ex) =>
        system.log.error("Failed to bind reverse proxy, terminating system", ex)
        system.terminate()
    }
  }
  //#start-http-server
  private def startHttpServer(routes: Route)(implicit system: ActorSystem[_]): Unit = {
    // Akka HTTP still needs a classic ActorSystem to start
    import system.executionContext

    val futureBinding = Http().newServerAt("localhost", 8080).bind(routes)
    futureBinding.onComplete {
      case Success(binding) =>
        val address = binding.localAddress
        system.log.info("Server online at http://{}:{}/", address.getHostString, address.getPort)
      case Failure(ex) =>
        system.log.error("Failed to bind HTTP endpoint, terminating system", ex)
        system.terminate()
    }
  }
  //#start-http-server
  def main(args: Array[String]): Unit = {
    //#server-bootstrapping
    val apiRoot = Behaviors.setup[Nothing] { context =>
      val userRegistryActor = context.spawn(UserRegistry(), "UserRegistryActor")
      context.watch(userRegistryActor)

      val routes = new UserRoutes(userRegistryActor)(context.system)
      startHttpServer(routes.userRoutes)(context.system)

      Behaviors.empty
    }

    val rProxyRoot = Behaviors.setup[Nothing] { context =>
      val loggingActor = context.spawn(LoggingActor(), "FirebaseLogger")
      context.watch(loggingActor)

      startReverseProxy(loggingActor)(context.system)

      Behaviors.empty
    }

    val apiSystem = ActorSystem[Nothing](apiRoot, "HelloAkkaHttpServer")
    val rProxySystem = ActorSystem[Nothing](rProxyRoot, "AkkaRProxyHttpServer", ConfigFactory.load().getConfig("r-proxy"))
    //#server-bootstrapping
  }
}
//#main-class
