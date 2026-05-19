package com.github.galleog.pekko.chapter06.routers

import org.apache.pekko.actor.typed.Behavior
import org.apache.pekko.actor.typed.javadsl.Behaviors
import org.apache.pekko.actor.typed.receptionist.ServiceKey

object PhotoProcessor {
    val KEY = ServiceKey.create(String::class.java, "photo-processor-key")

    fun create(): Behavior<String> = Behaviors.ignore()
}