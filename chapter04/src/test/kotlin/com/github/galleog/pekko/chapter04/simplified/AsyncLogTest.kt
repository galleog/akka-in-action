package com.github.galleog.pekko.chapter04.simplified

import org.apache.pekko.actor.testkit.typed.annotations.JUnit5TestKit
import org.apache.pekko.actor.testkit.typed.javadsl.ActorTestKit
import org.apache.pekko.actor.testkit.typed.javadsl.LogCapturingExtension
import org.apache.pekko.actor.testkit.typed.javadsl.LoggingTestKit
import org.apache.pekko.actor.testkit.typed.javadsl.TestKitJUnit5Extension
import org.apache.pekko.actor.typed.Behavior
import org.apache.pekko.actor.typed.javadsl.Behaviors
import org.junit.jupiter.api.extension.ExtendWith
import org.slf4j.event.Level
import kotlin.test.Test

@ExtendWith(TestKitJUnit5Extension::class, LogCapturingExtension::class)
class AsyncLogTest {
    @JUnit5TestKit
    private val testKit = ActorTestKit.create()

    @Test
    fun `a SimplifiedManager must be able to log 'it's done'`() {
        val manager = testKit.spawn(SimplifiedManager.create(), "manager")

        LoggingTestKit.info("it's done")
            .expect(testKit.system()) { manager.tell(SimplifiedManager.Log) }
    }

    @Test
    fun `log messages to dead letters`() {
        val behavior: Behavior<String> = Behaviors.stopped()

        val carl = testKit.spawn(behavior, "carl")

        LoggingTestKit.empty()
            .withLogLevel(Level.INFO)
            .withMessageRegex(".*Message.*to.*carl.*was not delivered.*2.*dead letters encountered")
            .expect(testKit.system()) {
                carl.tell("Hello")
                carl.tell("Hello")
            }
    }
}