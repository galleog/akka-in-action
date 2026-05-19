package com.github.galleog.pekko.chapter06.routers

import org.apache.pekko.actor.typed.ActorRef
import org.apache.pekko.actor.typed.Behavior
import org.apache.pekko.actor.typed.javadsl.AbstractBehavior
import org.apache.pekko.actor.typed.javadsl.ActorContext
import org.apache.pekko.actor.typed.javadsl.Behaviors
import org.apache.pekko.actor.typed.javadsl.Receive
import org.apache.pekko.actor.typed.javadsl.Routers

class DataObfuscator private constructor(context: ActorContext<Command>) :
    AbstractBehavior<DataObfuscator.Command>(context) {
    sealed interface Command
    data class Message(val id: String, val content: String) : Command

    private val router: ActorRef<Aggregator.Command> = context.spawnAnonymous(
        Routers.group(Aggregator.SERVICE_KEY)
            .withConsistentHashingRouting(10) { command -> command.id }
    )

    override fun createReceive(): Receive<Command> = newReceiveBuilder()
        .onMessage(Message::class.java, ::onMessage)
        .build()

    private fun onMessage(message: Message): Behavior<Command> {
        router.tell(Aggregator.Obfuscated(message.id, message.content.lowercase()))
        return this
    }

    companion object {
        fun create(): Behavior<Command> = Behaviors.setup(::DataObfuscator)
    }
}