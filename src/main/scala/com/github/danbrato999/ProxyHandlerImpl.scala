package com.github.danbrato999

import java.util.UUID

import akka.actor.typed.{ActorRef, ActorSystem}
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.Uri.{Authority, NamedHost}
import akka.http.scaladsl.model._
import akka.http.scaladsl.unmarshalling.Unmarshal
import com.typesafe.config.Config
import spray.json.{JsString, JsValue}

import scala.concurrent.Future
import scala.util.Success

class ProxyHandlerImpl(logger: ActorRef[ProxyMessage], config: Config)(implicit system: ActorSystem[_]) extends ProxyHandler {
  import SprayJsonSupport.sprayJsValueUnmarshaller
  import system.executionContext

  private val target = TargetServer(
    scheme = config.getString("scheme"),
    host = config.getString("host"),
    port = config.getInt("port")
  )
  private val http = Http(system)

  def processMsg(requestId: UUID, `type`: String, headers: Seq[HttpHeader], entity: HttpEntity): Future[Option[Any]] = (entity.contentType match {
    case ContentTypes.`application/json` => Unmarshal(entity).to[Option[JsValue]]
    case _ => Unmarshal(entity).to[Option[String]].map {
      case Some(value) => Some(JsString(value))
      case None => None
    }
  })
    .recover( _ => None)
    .andThen {
      case Success(body) =>
        val requestMsg = ProxyMessage(
          id = requestId,
          `type` = `type`,
          headers = headers.map(header => s"${header.name()}: ${header.value()}"),
          body = body
        )
        logger ! requestMsg
    }

  override def handle(request: HttpRequest): Future[HttpResponse] = {
    val requestId = UUID.randomUUID()
    val proxyRequest = request.withUri(
      request.uri.copy(
        scheme = target.scheme,
        authority = Authority(host = NamedHost(target.host), port = target.port)
      )
    )

    processMsg(requestId, "request", request.headers, request.entity)

    http.singleRequest(proxyRequest)
      .map(response => {
        processMsg(requestId, "response", response.headers, response.entity)
        response
      })
  }
}
