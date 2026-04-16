package com.github.galleog.pekko.chapter04.simplified

import org.apache.pekko.actor.typed.ActorRef
import org.apache.pekko.actor.typed.Behavior
import org.apache.pekko.actor.typed.javadsl.Behaviors
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

object SimplifiedManager {
    sealed interface Command
    data class CreateChild(val name: String) : Command
    data class Forward(val message: String, val sendTo: ActorRef<String>) : Command
    data object Log : Command
    data object ScheduleLog : Command

    fun create(): Behavior<Command> = Behaviors.receive { context, command ->
        when (command) {
            is CreateChild -> {
                context.spawn(SimplifiedWorker.create(), command.name)
                Behaviors.same()
            }

            is Forward -> {
                command.sendTo.tell(command.message)
                Behaviors.same()
            }

            is Log -> {
                context.log.info("it's done")
                Behaviors.same()
            }

            is ScheduleLog -> {
                context.scheduleOnce(1.seconds.toJavaDuration(), context.self, Log)
                Behaviors.same()
            }
        }
    }
}