package com.github.galleog.pekko.chapter06.routers

import org.apache.pekko.actor.testkit.typed.annotations.JUnit5TestKit
import org.apache.pekko.actor.testkit.typed.javadsl.ActorTestKit
import org.apache.pekko.actor.testkit.typed.javadsl.LogCapturingExtension
import org.apache.pekko.actor.testkit.typed.javadsl.TestKitJUnit5Extension
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.test.Test

@ExtendWith(TestKitJUnit5Extension::class, LogCapturingExtension::class)
class PoolRooterTest {
    @JUnit5TestKit
    private var testKit = ActorTestKit.create()

    @Test
    fun `should send messages in round-robin fashion`() {
        val probe = testKit.createTestProbe<String>()
        val worker = Worker.create(probe.ref)
        testKit.spawn(Manager.create(worker), "round-robin")

        probe.expectMessage("hi")
        probe.receiveSeveralMessages(10)
    }

    @Test
    fun `should broadcast, sending each message to all routtees`() {
        val probe = testKit.createTestProbe<String>()
        val worker = Worker.create(probe.ref)
        testKit.spawn(BroadcastingManager.create(worker), "broadcasting")

        probe.expectMessage("hi, there")
        probe.receiveSeveralMessages(43)
    }
}