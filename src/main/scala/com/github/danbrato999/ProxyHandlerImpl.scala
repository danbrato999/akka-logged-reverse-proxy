package com.github.danbrato999

import java.util.UUID

import akka.actor.typed.{ActorRef, ActorSystem}
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpHeader, HttpRequest, HttpResponse}
import akka.http.scaladsl.model.Uri.{Authority, NamedHost}
import akka.http.scaladsl.unmarshalling.Unmarshal
import spray.json.{JsString, JsValue}

import scala.concurrent.Future
import scala.util.Success

class ProxyHandlerImpl(logger: ActorRef[ProxyMessage])(implicit system: ActorSystem[_]) extends ProxyHandler {
  import system.executionContext
  import SprayJsonSupport.sprayJsValueUnmarshaller

  private val http = Http(system)

  def processMsg(requestId: UUID, headers: Seq[HttpHeader], entity: HttpEntity): Future[Option[Any]] = (entity.contentType match {
    case ContentTypes.`application/json` => Unmarshal(entity).to[Option[JsValue]]
    case _ => Unmarshal(entity).to[Option[String]].map {
      case Some(value) => Some(JsString(value))
      case None => None
    }
  })
    .recover( _ => None)
    .andThen {
      case Success(body) =>
        val requestMsg = ProxyMessage(requestId, headers.map(header => header.name() -> header.value()).toMap, body)
        logger ! requestMsg
    }

  override def handle(request: HttpRequest): Future[HttpResponse] = {
    val requestId = UUID.randomUUID()
    val proxyRequest = request.withUri(
      request.uri.copy(
        scheme = "http",
        authority = Authority(host = NamedHost("127.0.0.1"), port = 8080)
      )
    )

    processMsg(requestId, request.headers, request.entity)

    http.singleRequest(proxyRequest)
      .map(response => {
        processMsg(requestId, response.headers, response.entity)
        response
      })
  }
}
