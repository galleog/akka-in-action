package com.github.galleog.pekko.chapter04.simplified

import org.apache.pekko.actor.testkit.typed.annotations.JUnit5TestKit
import org.apache.pekko.actor.testkit.typed.javadsl.ActorTestKit
import org.apache.pekko.actor.testkit.typed.javadsl.LogCapturingExtension
import org.apache.pekko.actor.testkit.typed.javadsl.TestKitJUnit5Extension
import org.apache.pekko.actor.typed.javadsl.Behaviors
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.test.Test

@ExtendWith(TestKitJUnit5Extension::class, LogCapturingExtension::class)
class AsyncForwardTest {
    @JUnit5TestKit
    private val testKit = ActorTestKit.create()

    @Test
    fun `actor gets forwarded message from manager`() {
        val manager = testKit.spawn(SimplifiedManager.create())
        val probe = testKit.createTestProbe<String>()
        manager.tell(SimplifiedManager.Forward("message-to-parse", probe.ref))
        probe.expectMessage("message-to-parse")
    }

    @Test
    fun `a monitor must intercept the messages`() {
        val probe = testKit.createTestProbe<String>()
        val behaviorUnderTest = Behaviors.receiveMessage<String> { Behaviors.ignore() }
        val behaviorMonitored = Behaviors.monitor(String::class.java, probe.ref, behaviorUnderTest)
        val actor = testKit.spawn(behaviorMonitored)

        actor.tell("checking")
        probe.expectMessage("checking")
    }
}