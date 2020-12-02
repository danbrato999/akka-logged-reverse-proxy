package com.github.danbrato999

import java.util.UUID

import spray.json.JsValue

final case class ProxyMessage(id: UUID, headers: Map[String, String], body: Option[JsValue] = None)
