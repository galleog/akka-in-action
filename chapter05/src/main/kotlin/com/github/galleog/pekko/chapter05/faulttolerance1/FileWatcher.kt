package com.github.galleog.pekko.chapter05.faulttolerance1

import com.github.galleog.pekko.chapter05.faulttolerance1.exception.CorruptedFileException
import org.apache.pekko.actor.typed.ActorRef
import org.apache.pekko.actor.typed.SupervisorStrategy
import org.apache.pekko.actor.typed.javadsl.Behaviors
import java.io.File

object FileWatcher {
    sealed interface Command
    data class NewFile(val file: File, val timeAdded: Long) : Command

    fun create(directory: String, logProcessor: ActorRef<LogProcessor.Command>) = Behaviors.supervise(
        Behaviors.setup<Command> {
            Behaviors.receiveMessage { message ->
                when (message) {
                    is NewFile -> {
                        //sends file to log processor
                        TODO("Not yet implemented")
                    }
                }
            }
        }
    ).onFailure(CorruptedFileException::class.java, SupervisorStrategy.restart())
}