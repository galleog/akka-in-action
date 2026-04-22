package com.github.galleog.pekko.chapter05

import org.apache.pekko.actor.typed.Behavior
import org.apache.pekko.actor.typed.PostStop
import org.apache.pekko.actor.typed.javadsl.BehaviorBuilder
import org.apache.pekko.actor.typed.javadsl.Behaviors

object SupervisionExample {
    fun create(): Behavior<String> = Behaviors.setup { context ->
        BehaviorBuilder.create<String>()
            .onMessageEquals("secret") {
                context.log.info("granted")
                Behaviors.same()
            }.onMessageEquals("stop") {
                context.log.info("stopping")
                Behaviors.stopped()
            }.onMessageEquals("recoverable") {
                context.log.info("recoverable")
                throw IllegalStateException()
            }.onMessageEquals("fatal") {
                throw OutOfMemoryError()
            }.onSignal(PostStop::class.java) {
                cleaning()
                Behaviors.same()
            }.build()
    }

    fun cleaning() {
        // clean resources
    }
}