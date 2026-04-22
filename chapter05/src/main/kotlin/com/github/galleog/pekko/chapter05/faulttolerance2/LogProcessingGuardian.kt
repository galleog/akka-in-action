package com.github.galleog.pekko.chapter05.faulttolerance2

import org.apache.pekko.actor.typed.ActorRef
import org.apache.pekko.actor.typed.ActorSystem
import org.apache.pekko.actor.typed.Behavior
import org.apache.pekko.actor.typed.Terminated
import org.apache.pekko.actor.typed.javadsl.BehaviorBuilder
import org.apache.pekko.actor.typed.javadsl.Behaviors

object LogProcessingGuardian {
    fun create(directories: List<String>): Behavior<Nothing> = Behaviors.setup { context ->
        for (directory in directories) {
            val fileWatcher: ActorRef<FileWatcher.Command> =
                context.spawnAnonymous(FileWatcher.create(directory))
            context.watch(fileWatcher)
        }

        BehaviorBuilder.create<Nothing>()
            .onSignal(Terminated::class.java) {
                // checks not all fileWatcher had Terminated
                // if no fileWatcher left shuts down the system
                Behaviors.same()
            }.build()
    }
}

fun main() {
    // this directories may come from settings or from args
    val directories = listOf("file:///source1/", "file:///source2/")

    val guardian = ActorSystem.create(LogProcessingGuardian.create(directories), "log-processing-guardian")
}