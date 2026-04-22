package com.github.galleog.pekko.chapter05

import org.apache.pekko.actor.typed.ActorRef
import org.apache.pekko.actor.typed.Behavior
import org.apache.pekko.actor.typed.Terminated
import org.apache.pekko.actor.typed.javadsl.BehaviorBuilder
import org.apache.pekko.actor.typed.javadsl.Behaviors

object SimplifiedFileWatcher {
    sealed interface Command
    data class Watch(val ref: ActorRef<String>) : Command

    fun create(): Behavior<Command> = Behaviors.setup { context ->
        BehaviorBuilder.create<Command>()
            .onMessage(Watch::class.java) { message ->
                context.watch(message.ref)
                Behaviors.same()
            }.onSignal(Terminated::class.java) {
                context.log.info("terminated")
                Behaviors.same()
            }.build()
    }
}