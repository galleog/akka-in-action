package com.github.galleog.pekko.chapter05.faulttolerance2

import com.github.galleog.pekko.chapter05.faulttolerance2.exception.DbBrokenConnectionException
import com.github.galleog.pekko.chapter05.faulttolerance2.exception.UnexpectedColumnsException
import org.apache.pekko.actor.typed.Behavior
import org.apache.pekko.actor.typed.PostStop
import org.apache.pekko.actor.typed.PreRestart
import org.apache.pekko.actor.typed.SupervisorStrategy
import org.apache.pekko.actor.typed.javadsl.BehaviorBuilder
import org.apache.pekko.actor.typed.javadsl.Behaviors
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

object DbWriter {
    sealed interface Command
    data class Line(val time: Long, val message: String, val messageType: String) : Command

    fun create(databaseUrl: String): Behavior<Command> = superviseStrategy(
        Behaviors.setup {
            // creates connection using databaseUrl

            BehaviorBuilder.create<Command>()
                .onMessage(Line::class.java) {
                    // transforms line to db schema and saves to db
                    TODO("Not yet implemented")
                }.onSignal(PostStop::class.java) {
                    //close connection
                    TODO("Not yet implemented")
                }.onSignal(PreRestart::class.java) {
                    //close connection
                    TODO("Not yet implemented")
                }.build()
        }
    )

    private fun superviseStrategy(behavior: Behavior<Command>): Behavior<Command> = Behaviors.supervise(
        Behaviors.supervise(behavior)
            .onFailure(UnexpectedColumnsException::class.java, SupervisorStrategy.resume())
    ).onFailure(
        DbBrokenConnectionException::class.java, SupervisorStrategy.restartWithBackoff(
            3.seconds.toJavaDuration(), 30.seconds.toJavaDuration(), 0.1
        ).withResetBackoffAfter(15.seconds.toJavaDuration())
    )
}