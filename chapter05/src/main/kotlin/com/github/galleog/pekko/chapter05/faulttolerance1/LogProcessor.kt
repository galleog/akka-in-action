package com.github.galleog.pekko.chapter05.faulttolerance1

import com.github.galleog.pekko.chapter05.faulttolerance1.exception.CorruptedFileException
import org.apache.pekko.actor.typed.ActorRef
import org.apache.pekko.actor.typed.SupervisorStrategy
import org.apache.pekko.actor.typed.javadsl.Behaviors
import java.io.File

object LogProcessor {
    sealed interface Command
    data class LogFile(val file: File) : Command

    fun create(dbWriter: ActorRef<DbWriter.Command>) = Behaviors.supervise(
        Behaviors.receiveMessage<Command> { message ->
            when (message) {
                is LogFile -> {
                    // parses file and sends each line to dbWriter
                    TODO("Not yet implemented")
                }
            }
        }
    ).onFailure(CorruptedFileException::class.java, SupervisorStrategy.resume())
}