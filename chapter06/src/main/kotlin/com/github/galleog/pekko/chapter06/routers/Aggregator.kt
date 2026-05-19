package com.github.galleog.pekko.chapter06.routers

import org.apache.pekko.actor.typed.ActorRef
import org.apache.pekko.actor.typed.Behavior
import org.apache.pekko.actor.typed.javadsl.AbstractBehavior
import org.apache.pekko.actor.typed.javadsl.ActorContext
import org.apache.pekko.actor.typed.javadsl.Behaviors
import org.apache.pekko.actor.typed.javadsl.Receive
import org.apache.pekko.actor.typed.receptionist.Receptionist
import org.apache.pekko.actor.typed.receptionist.ServiceKey

class Aggregator private constructor(
    private val messages: Map<String, String>,
    private val forwardTo: ActorRef<Event>,
    context: ActorContext<Command>
) : AbstractBehavior<Aggregator.Command>(context) {
    sealed interface Command {
        val id: String
    }

    data class Obfuscated(override val id: String, val content: String) : Command
    data class Enriched(override val id: String, val metadata: String) : Command

    sealed interface Event {
        val id: String
    }

    data class Completed(override val id: String, val content: String, val metadata: String) : Event

    override fun createReceive(): Receive<Command> = newReceiveBuilder()
        .onMessage(Obfuscated::class.java, ::onObfuscated)
        .onMessage(Enriched::class.java, ::onEnriched)
        .build()

    private fun onObfuscated(message: Obfuscated): Behavior<Command> {
        val existing = messages[message.id]
        if (existing != null) {
            forwardTo.tell(Completed(message.id, message.content, existing))
            return create(messages - message.id, forwardTo)
        } else {
            return create(messages + (message.id to message.content), forwardTo)
        }
    }

    private fun onEnriched(message: Enriched): Behavior<Command> {
        val existing = messages[message.id]
        if (existing != null) {
            forwardTo.tell(Completed(message.id, existing, message.metadata))
            return create(messages - message.id, forwardTo)
        } else {
            return create(messages + (message.id to message.metadata), forwardTo)
        }
    }

    companion object {
        val SERVICE_KEY = ServiceKey.create(Command::class.java, "agg-key")

        fun create(messages: Map<String, String> = emptyMap(), forwardTo: ActorRef<Event>): Behavior<Command> =
            Behaviors.setup { context ->
                context.system.receptionist().tell(Receptionist.register(SERVICE_KEY, context.self))
                Aggregator(messages, forwardTo, context)
            }
    }
}