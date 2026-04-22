package com.github.galleog.pekko.chapter05

import org.apache.pekko.actor.typed.ActorRef
import org.apache.pekko.actor.typed.Behavior
import org.apache.pekko.actor.typed.ChildFailed
import org.apache.pekko.actor.typed.Terminated
import org.apache.pekko.actor.typed.javadsl.BehaviorBuilder
import org.apache.pekko.actor.typed.javadsl.Behaviors

object ParentWatcher {
    sealed interface Command
    data class Spawn(val behavior: Behavior<String>) : Command
    data object StopChildren : Command
    data object FailChildren : Command

    val childBehavior: Behavior<String> = Behaviors.receive { _, message ->
        when (message) {
            "stop" -> Behaviors.stopped()
            "exception" -> throw Exception()
            "error" -> throw OutOfMemoryError()
            else -> Behaviors.same()
        }
    }

    fun create(monitor: ActorRef<String>, children: List<ActorRef<String>> = emptyList()): Behavior<Command> =
        Behaviors.setup { context ->
            BehaviorBuilder.create<Command>()
                .onMessage(Spawn::class.java) { message ->
                    val child = context.spawnAnonymous(message.behavior)
                    context.watch(child)
                    create(monitor, children + child)
                }.onMessage(StopChildren::class.java) {
                    for (child in children) child.tell("stop")
                    Behaviors.same()
                }.onMessage(FailChildren::class.java) {
                    for (child in children) child.tell("exception")
                    Behaviors.same()
                }.onSignal(ChildFailed::class.java) {
                    monitor.tell("childFailed")
                    Behaviors.same()
                }.onSignal(Terminated::class.java) {
                    monitor.tell("terminated")
                    Behaviors.same()
                }.build()
        }
}