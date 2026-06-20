package com.github.galleog.pekko.chapter10c

import org.apache.pekko.actor.typed.Behavior
import org.apache.pekko.actor.typed.javadsl.Behaviors

object LoggerBehavior {
    fun create(tag: String): Behavior<Unit> = Behaviors.setup { context ->
        context.log.info("spawned LoggerBehavior {}", tag)
        Behaviors.ignore()
    }
}