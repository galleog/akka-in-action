package com.github.galleog.pekko.chapter04.fishing

import org.apache.pekko.actor.typed.Behavior
import org.apache.pekko.actor.typed.javadsl.Behaviors
import java.time.Duration

object CounterTimer {
    sealed interface Command
    data object Increase : Command
    data class Pause(val seconds: Int) : Command
    data object Resume : Command

    fun create(): Behavior<Command> = resume(0)

    fun resume(count: Int): Behavior<Command> = Behaviors.receive { context, command ->
        Behaviors.withTimers { timers ->
            when (command) {
                is Increase -> {
                    val current = count + 1
                    context.log.info("Increaing to $current")
                    resume(current)
                }

                is Pause -> {
                    timers.startSingleTimer(Resume, Duration.ofSeconds(command.seconds.toLong()))
                    pause(count)
                }

                is Resume -> Behaviors.same()
            }
        }
    }

    fun pause(count: Int): Behavior<Command> = Behaviors.receive { context, command ->
        when (command) {
            is Increase -> {
                context.log.info("Counter is paused. Can't increase")
                Behaviors.same()
            }

            is Pause -> {
                context.log.info("Counter is paused. Can't pause again")
                Behaviors.same()
            }

            is Resume -> {
                context.log.info("Resuming")
                resume(count)
            }
        }
    }
}