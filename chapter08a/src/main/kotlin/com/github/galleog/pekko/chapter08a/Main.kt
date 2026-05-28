package com.github.galleog.pekko.chapter08a

import org.apache.pekko.actor.typed.ActorSystem
import org.apache.pekko.actor.typed.javadsl.Behaviors
import org.apache.pekko.management.javadsl.PekkoManagement

fun main() {
    val system: ActorSystem<Unit> = ActorSystem.create(Behaviors.empty(), "words")
    PekkoManagement.get(system).start()
}