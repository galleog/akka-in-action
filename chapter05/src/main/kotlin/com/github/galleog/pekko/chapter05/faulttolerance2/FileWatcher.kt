package com.github.galleog.pekko.chapter05.faulttolerance2

import com.github.galleog.pekko.chapter05.faulttolerance2.exception.ClosedWatchServiceException
import org.apache.pekko.actor.typed.SupervisorStrategy
import org.apache.pekko.actor.typed.Terminated
import org.apache.pekko.actor.typed.javadsl.BehaviorBuilder
import org.apache.pekko.actor.typed.javadsl.Behaviors
import java.io.File
import kotlin.jvm.java

object FileWatcher : FileListeningAbilities {
    sealed interface Command
    data class NewFile(val file: File, val timeAdded: Long) : Command
    data class FileModified(val file: File, val timeModified: Long) : Command

    override fun register(uri: String) {
        TODO("Not yet implemented")
    }

    fun create(directory: String) = Behaviors.supervise(
        Behaviors.setup {
            // starts listening to directory, spawns and watches a LogProcessor
            // when new file in the directory it receives NewFile
            // when modified file in the directory it receives FileModified
            BehaviorBuilder.create<Command>()
                .onMessage(NewFile::class.java) {
                    // sends file to log processor
                    TODO("Not yet implemented")
                }.onMessage(FileModified::class.java) {
                    //sends event to log processor
                    TODO("Not yet implemented")
                }.onSignal(Terminated::class.java) {
                    // stops itself as LogProcessor will be unresumable
                    TODO("Not yet implemented")
                }.build()
        }
    ).onFailure(ClosedWatchServiceException::class.java, SupervisorStrategy.restart())
}