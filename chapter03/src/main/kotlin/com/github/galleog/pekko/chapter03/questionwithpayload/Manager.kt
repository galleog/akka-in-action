package com.github.galleog.pekko.chapter03.questionwithpayload

import org.apache.pekko.actor.typed.ActorRef
import org.apache.pekko.actor.typed.Behavior
import org.apache.pekko.actor.typed.javadsl.Behaviors
import java.time.Duration

object Manager {
    sealed interface Command
    data class Delegate(val text: List<String>) : Command
    private data class Report(val outline: String) : Command

    fun create(): Behavior<Command> {
        fun auxCreateRequest(text: String): (ActorRef<Worker.Response>) -> Worker.Command =
            { replyTo -> Worker.Parse(text, replyTo) }

        return Behaviors.receive { context, command ->
            when (command) {
                is Delegate -> {
                    command.text.forEach { text ->
                        val worker: ActorRef<Worker.Command> = context.spawn(Worker.create(), "worker-$text")
                        context.log.info("Sending '$text' to worker")
                        context.ask(
                            Worker.Response::class.java,
                            worker,
                            Duration.ofSeconds(3),
                            auxCreateRequest(text)
                        ) { response, throwable ->
                            if (response != null) Report("'$text' read by ${worker.path().name()}")
                            else Report("Parsing '$text' has failed with [${throwable.message}]")
                        }
                    }
                    Behaviors.same()
                }

                is Report -> {
                    context.log.info(command.outline)
                    Behaviors.same()
                }
            }
        }
    }
}