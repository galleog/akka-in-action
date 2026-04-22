package com.github.galleog.pekko.chapter05

import org.apache.pekko.actor.testkit.typed.annotations.JUnit5TestKit
import org.apache.pekko.actor.testkit.typed.javadsl.ActorTestKit
import org.apache.pekko.actor.testkit.typed.javadsl.LogCapturingExtension
import org.apache.pekko.actor.testkit.typed.javadsl.TestKitJUnit5Extension
import org.apache.pekko.actor.typed.ActorRef
import org.apache.pekko.actor.typed.Behavior
import org.apache.pekko.actor.typed.SupervisorStrategy
import org.apache.pekko.actor.typed.javadsl.Behaviors
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.test.Test

@ExtendWith(TestKitJUnit5Extension::class, LogCapturingExtension::class)
class CheckingMessagesTest {
    @JUnit5TestKit
    private val testKit = ActorTestKit.create()

    @Test
    fun `an actor that restarts should not reprocess the message that failed`() {
        val probe = testKit.createTestProbe<Int>()
        val actor = testKit.spawn(behavior(probe.ref))

        for (i in 1..10) {
            actor.tell(i)
        }

        probe.expectMessage(2)
        probe.expectNoMessage()
    }

    fun behavior(monitor: ActorRef<Int>): Behavior<Int> = Behaviors.supervise(
        Behaviors.receive<Int> { _, message ->
            when (message) {
                2 -> {
                    monitor.tell(2)
                    throw IllegalArgumentException(2.toString())
                }

                else -> Behaviors.same()
            }
        }
    ).onFailure(SupervisorStrategy.restart())
}