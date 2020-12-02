package com.github.danbrato999

import java.util.UUID

import spray.json.JsValue

import scala.beans.BeanProperty
import scala.jdk.CollectionConverters.SeqHasAsJava


final case class ProxyMessage(id: UUID, `type`: String, headers: Seq[String], body: Option[JsValue] = None) {
  def toBean: ProxyMessageBean = {
    val bean =  new ProxyMessageBean(
      headers = headers.toList.asJava,
      body = body match {
        case Some(jsValue) => jsValue.prettyPrint
        case None => ""
      }
    )

    bean
  }
}

class ProxyMessageBean(
  @BeanProperty var headers: java.util.List[String],
  @BeanProperty var body: String
)