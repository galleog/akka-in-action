package com.github.galleog.pekko.chapter05.faulttolerance1

import org.apache.pekko.actor.typed.ActorRef
import org.apache.pekko.actor.typed.ActorSystem
import org.apache.pekko.actor.typed.Behavior
import org.apache.pekko.actor.typed.Terminated
import org.apache.pekko.actor.typed.javadsl.BehaviorBuilder
import org.apache.pekko.actor.typed.javadsl.Behaviors

object LogProcessingGuardian {
    fun create(sources: List<String>, databaseUrl: String): Behavior<Nothing> = Behaviors.setup { context ->
        for (source in sources) {
            val dbWriter: ActorRef<DbWriter.Command> = context.spawnAnonymous(DbWriter.create(databaseUrl))
            // wouldn't it be better to have more log processors
            val logProcessor: ActorRef<LogProcessor.Command> = context.spawnAnonymous(LogProcessor.create(dbWriter))
            val fileWatcher: ActorRef<FileWatcher.Command> =
                context.spawnAnonymous(FileWatcher.create(source, logProcessor))
            context.watch(fileWatcher)
        }

        BehaviorBuilder.create<Nothing>()
            .onSignal(Terminated::class.java) {
                // checks there is some fileWatcher running
                // if there's no fileWatcher left then shutsdown the system
                Behaviors.same()
            }.build()
    }
}

fun main() {
    val sources = listOf("file:///source1/", "file:///source2/")
    val databaseUrl = "http://mydatabase1"

    val guardian = ActorSystem.create(LogProcessingGuardian.create(sources, databaseUrl), "log-processing-guardian")
}