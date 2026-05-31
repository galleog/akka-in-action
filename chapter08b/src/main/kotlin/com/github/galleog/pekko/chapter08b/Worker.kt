package com.github.galleog.pekko.chapter08b

import org.apache.pekko.actor.typed.ActorRef
import org.apache.pekko.actor.typed.Behavior
import org.apache.pekko.actor.typed.javadsl.Behaviors
import org.apache.pekko.actor.typed.receptionist.Receptionist
import org.apache.pekko.actor.typed.receptionist.ServiceKey

object Worker {
    sealed interface Command
    data class Process(val text: String, val replyTo: ActorRef<Master.Event>) : Command, CborSerializable

    val REGISTRATION_KEY = ServiceKey.create(Command::class.java, "Worker")

    fun create(): Behavior<Command> = Behaviors.setup { context ->
        context.log.debug("{} subscribing to {}", context.self, REGISTRATION_KEY)
        context.system.receptionist().tell(Receptionist.register(REGISTRATION_KEY, context.self))

        Behaviors.receiveMessage { message ->
            when (message) {
                is Process -> {
                    context.log.debug("processing {}", message.text)
                    message.replyTo.tell(Master.CountedWords(processTask(message.text)))
                    Behaviors.same()
                }
            }
        }
    }

    fun processTask(text: String): Map<String, Int> {
        val res = mutableMapOf<String, Int>()
        for (word in text.split("\\W+".toRegex())) {
            res[word] = res.getOrDefault(word, 0) + 1
        }
        return res
    }
}