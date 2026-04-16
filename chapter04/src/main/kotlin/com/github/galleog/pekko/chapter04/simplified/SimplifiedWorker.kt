package com.github.galleog.pekko.chapter04.simplified

import org.apache.pekko.actor.typed.Behavior
import org.apache.pekko.actor.typed.javadsl.Behaviors

object SimplifiedWorker {
    fun create(): Behavior<String> = Behaviors.ignore()
}