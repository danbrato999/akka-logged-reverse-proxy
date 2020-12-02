package com.github.danbrato999

import akka.actor.typed.{ActorRef, ActorSystem}
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}

import scala.concurrent.Future

trait ProxyHandler {
  def handle(request: HttpRequest): Future[HttpResponse]
}

object ProxyHandler {
  def apply(logger: ActorRef[ProxyMessage])(implicit system: ActorSystem[_]) = new ProxyHandlerImpl(logger)
}

