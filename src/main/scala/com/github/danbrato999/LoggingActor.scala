package com.github.danbrato999

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.{FirebaseApp, FirebaseOptions}
import com.typesafe.config.ConfigFactory
import spray.json.enrichAny

object LoggingActor {
  import JsonFormats.proxyMessageFormat
  private val config = ConfigFactory.load().getConfig("r-proxy")

  private val credentials = getClass.getResourceAsStream("/firebaseCredentials.json")
  private val options = FirebaseOptions.builder()
    .setCredentials(GoogleCredentials.fromStream(credentials))
    .setDatabaseUrl(config.getString("firebase.database"))
    .build()

  FirebaseApp.initializeApp(options)

  private val db = FirebaseDatabase.getInstance()
    .getReference("/proxy_logs")

  def apply(): Behavior[ProxyMessage] = Behaviors.receive { (context, msg) =>
    context.log.info("Message Received -> {}", msg.toJson)
    db.child(msg.id.toString).child(msg.`type`).setValueAsync(msg.toBean)
    Behaviors.same
  }
}
