package com.github.danbrato999

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import spray.json.enrichAny

object LoggingActor {
  import JsonFormats.proxyMessageFormat

  def apply(): Behavior[ProxyMessage] = Behaviors.receive { (context, msg) =>
    context.log.info("Message Received -> {}", msg.toJson)
    Behaviors.same
  }
}
