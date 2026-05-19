package com.github.galleog.pekko.chapter06.routers

import org.apache.pekko.actor.typed.ActorRef
import org.apache.pekko.actor.typed.Behavior
import org.apache.pekko.actor.typed.javadsl.Behaviors

object Switch {
    sealed interface Command
    data object SwitchOn : Command
    data object SwitchOff : Command
    data class Payload(val content: String, val metadata: String) : Command

    fun create(forwardTo: ActorRef<String>, alertTo: ActorRef<String>): Behavior<Command> = on(forwardTo, alertTo)

    private fun on(forwardTo: ActorRef<String>, alertTo: ActorRef<String>): Behavior<Command> =
        Behaviors.receive { context, message ->
            when (message) {
                is SwitchOn -> {
                    context.log.warn("sent SwitchOn but was ON already")
                    Behaviors.same()
                }

                is SwitchOff -> off(forwardTo, alertTo)

                is Payload -> {
                    forwardTo.tell(message.content)
                    Behaviors.same()
                }
            }
        }

    private fun off(forwardTo: ActorRef<String>, alertTo: ActorRef<String>): Behavior<Command> =
        Behaviors.receive { context, message ->
            when (message) {
                is SwitchOn -> on(forwardTo, alertTo)

                is SwitchOff -> {
                    context.log.warn("sent SwitchOff but was OFF already")
                    Behaviors.same()
                }

                is Payload -> {
                    alertTo.tell(message.metadata)
                    Behaviors.same()
                }
            }
        }
}