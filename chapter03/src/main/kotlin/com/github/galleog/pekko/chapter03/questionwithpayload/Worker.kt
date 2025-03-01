package com.github.galleog.pekko.chapter03.questionwithpayload

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.apache.pekko.actor.typed.ActorRef
import org.apache.pekko.actor.typed.Behavior
import org.apache.pekko.actor.typed.javadsl.Behaviors
import kotlin.random.Random

object Worker {
    sealed interface Command
    data class Parse(val text: String, val replyTo: ActorRef<Response>) : Command

    sealed interface Response
    data object Done : Response

    fun create(): Behavior<Command> = Behaviors.receive { context, command ->
        when (command) {
            is Parse -> {
                fakeLengthyParsing(command.text)
                context.log.info("${context.self.path().name()}: done")
                command.replyTo.tell(Done)
                Behaviors.stopped()
            }
        }
    }

    private fun fakeLengthyParsing(text: String) = runBlocking {
        delay(Random.nextLong(2000, 4000))
        text
    }
}