package com.github.danbrato999

import java.util.UUID

import com.github.danbrato999.api.UserRegistry.ActionPerformed
import com.github.danbrato999.api.{User, Users}
import spray.json.{DefaultJsonProtocol, DeserializationException, JsString, JsValue, RootJsonFormat}

object JsonFormats {
  // import the default encoders for primitive types (Int, String, Lists etc)

  import DefaultJsonProtocol._

  implicit val userJsonFormat = jsonFormat3(User)
  implicit val usersJsonFormat = jsonFormat1(Users)

  implicit val actionPerformedJsonFormat = jsonFormat1(ActionPerformed)

  implicit object UUIDFormat extends RootJsonFormat[UUID] {
    def write(uuid: UUID): JsString = JsString(uuid.toString)

    def read(value: JsValue): UUID = value match {
      case JsString(uuid) => UUID.fromString(uuid)
      case _ => throw DeserializationException("Expected hexadecimal UUID string")
    }
  }

  implicit val proxyMessageFormat = jsonFormat3(ProxyMessage)
}
