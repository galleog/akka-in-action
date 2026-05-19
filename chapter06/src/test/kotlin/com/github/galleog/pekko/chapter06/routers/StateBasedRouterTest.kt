package com.github.galleog.pekko.chapter06.routers

import org.apache.pekko.actor.testkit.typed.annotations.JUnit5TestKit
import org.apache.pekko.actor.testkit.typed.javadsl.ActorTestKit
import org.apache.pekko.actor.testkit.typed.javadsl.LogCapturingExtension
import org.apache.pekko.actor.testkit.typed.javadsl.TestKitJUnit5Extension
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.test.Test

@ExtendWith(TestKitJUnit5Extension::class, LogCapturingExtension::class)
class StateBasedRouterTest {
    @JUnit5TestKit
    private val testKit = ActorTestKit.create()

    private val forwardToProbe = testKit.createTestProbe<String>()
    private val alertToProbe = testKit.createTestProbe<String>()
    private val switch = testKit.spawn(Switch.create(forwardToProbe.ref, alertToProbe.ref), "switch")

    @Test
    fun `should route to forward to actor reference when on`() {
        switch.tell(Switch.SwitchOn)
        switch.tell(Switch.Payload("content1", "metadata1"))
        forwardToProbe.expectMessage("content1")
    }

    @Test
    fun `should route to alert actor and wait when off`() {
        switch.tell(Switch.SwitchOff)
        switch.tell(Switch.Payload("content2", "metadata2"))
        alertToProbe.expectMessage("metadata2")
    }
}