package com.github.galleog.pekko.chapter03.errorkernel

import org.apache.pekko.actor.typed.ActorRef
import org.apache.pekko.actor.typed.Behavior
import org.apache.pekko.actor.typed.javadsl.Behaviors

object Worker {
    sealed interface Command
    data class Parse(val replyTo: ActorRef<Response>, val text: String) : Command

    sealed interface Response
    data class Done(val text: String) : Response

    fun create(): Behavior<Command> = Behaviors.receive { context, command ->
        when (command) {
            is Parse -> {
                val parsed = naiveParsing(command.text)
                context.log.info("Worker '${context.self.path()}' DONE! Parsed result: $parsed")
                command.replyTo.tell(Done(parsed))
                Behaviors.stopped()
            }
        }
    }

    private fun naiveParsing(text: String) = text.replace("-", "")
}