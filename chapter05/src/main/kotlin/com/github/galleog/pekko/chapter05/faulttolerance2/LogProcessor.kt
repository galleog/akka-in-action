package com.github.galleog.pekko.chapter05.faulttolerance2

import com.github.galleog.pekko.chapter05.faulttolerance2.exception.ParseException
import org.apache.pekko.actor.typed.ActorRef
import org.apache.pekko.actor.typed.SupervisorStrategy
import org.apache.pekko.actor.typed.Terminated
import org.apache.pekko.actor.typed.javadsl.BehaviorBuilder
import org.apache.pekko.actor.typed.javadsl.Behaviors
import java.io.File

object LogProcessor {
    sealed interface Command
    data class LogFile(val file: File) : Command

    fun create(dbWriter: ActorRef<DbWriter.Command>) = Behaviors.supervise(
        Behaviors.setup {
            // spawns dbWriter with url from settings
            // watches dbWriter

            BehaviorBuilder.create<Command>()
                .onMessage(LogFile::class.java) {
                    // reads file. parses by line, sends cleaned line to dbWriter
                    TODO("Not yet implemented")
                }.onSignal(Terminated::class.java) {
                    // recreates the dbWriter or stops itself
                    TODO("Not yet implemented")
                }.build()
        }
    ).onFailure(ParseException::class.java, SupervisorStrategy.resume())
}