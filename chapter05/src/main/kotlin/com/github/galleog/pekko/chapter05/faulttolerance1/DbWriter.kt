package com.github.galleog.pekko.chapter05.faulttolerance1

import com.github.galleog.pekko.chapter05.faulttolerance1.exception.DbBrokenConnectionException
import com.github.galleog.pekko.chapter05.faulttolerance1.exception.DbNodeDownException
import org.apache.pekko.actor.typed.Behavior
import org.apache.pekko.actor.typed.PostStop
import org.apache.pekko.actor.typed.PreRestart
import org.apache.pekko.actor.typed.SupervisorStrategy
import org.apache.pekko.actor.typed.javadsl.BehaviorBuilder
import org.apache.pekko.actor.typed.javadsl.Behaviors

object DbWriter {
    sealed interface Command
    data class Line(val time: Long, val message: String, val messageType: String) : Command

    fun create(databaseUrl: String): Behavior<Command> = superviseStrategy(
        Behaviors.setup {
            // creates connection with databaseUrl

            BehaviorBuilder.create<Command>()
                .onMessage(Line::class.java) {
                    // saves line to db
                    TODO("Not yet implemented")
                }.onSignal(PostStop::class.java) {
                    // closes connection
                    TODO("Not yet implemented")
                }.onSignal(PreRestart::class.java) {
                    // closes connection
                    TODO("Not yet implemented")
                }.build()
        }
    )

    private fun superviseStrategy(behavior: Behavior<Command>): Behavior<Command> = Behaviors.supervise(
        Behaviors.supervise(behavior)
            .onFailure(DbBrokenConnectionException::class.java, SupervisorStrategy.restart())
    ).onFailure(DbNodeDownException::class.java, SupervisorStrategy.stop())
}