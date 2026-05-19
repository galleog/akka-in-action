package com.github.galleog.pekko.chapter06.routers

import org.apache.pekko.actor.testkit.typed.annotations.JUnit5TestKit
import org.apache.pekko.actor.testkit.typed.javadsl.ActorTestKit
import org.apache.pekko.actor.testkit.typed.javadsl.LogCapturingExtension
import org.apache.pekko.actor.testkit.typed.javadsl.TestKitJUnit5Extension
import org.apache.pekko.actor.typed.javadsl.Behaviors
import org.apache.pekko.actor.typed.receptionist.Receptionist
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.test.Test

@ExtendWith(TestKitJUnit5Extension::class, LogCapturingExtension::class)
class GroupRouterTest {
    @JUnit5TestKit
    private val testKit = ActorTestKit.create()

    @Test
    fun `should send messages to one worker registered at a key`() {
        val probe = testKit.createTestProbe<String>()
        val behavior = Behaviors.monitor(String::class.java, probe.ref, Behaviors.empty())

        testKit.system().receptionist().tell(Receptionist.register(PhotoProcessor.KEY, testKit.spawn(behavior)))

        val groupRouter = testKit.spawn(Camera.create())
        groupRouter.tell(Camera.Photo("hi"))

        probe.expectMessage("hi")
    }

    @Test
    fun `should send messages to all photo processors registered`() {
        val probe1 = testKit.createTestProbe<String>()
        val pp1Monitor = Behaviors.monitor(String::class.java, probe1.ref, PhotoProcessor.create())

        val probe2 = testKit.createTestProbe<String>()
        val pp2Monitor = Behaviors.monitor(String::class.java, probe2.ref, PhotoProcessor.create())

        testKit.system().receptionist().tell(Receptionist.register(PhotoProcessor.KEY, testKit.spawn(pp1Monitor)))
        testKit.system().receptionist().tell(Receptionist.register(PhotoProcessor.KEY, testKit.spawn(pp2Monitor)))

        val camera = testKit.spawn(Camera.create())
        camera.tell(Camera.Photo("A"))
        camera.tell(Camera.Photo("B"))

        probe1.receiveSeveralMessages(1)
        probe2.receiveSeveralMessages(1)
    }

    @Test
    fun `should send messages with same id to the same aggregator`() {
        val probe1 = testKit.createTestProbe<Aggregator.Event>()
        val probe2 = testKit.createTestProbe<Aggregator.Event>()

        testKit.spawn(Aggregator.create(forwardTo = probe1.ref), "aggregator1")
        testKit.spawn(Aggregator.create(forwardTo = probe2.ref), "aggregator2")

        val contentValidator = testKit.spawn(DataObfuscator.create(), "wa-1")
        val dataEnricher = testKit.spawn(DataEnricher.create(), "wb-1")

        // when a message with same id is sent to different actors
        contentValidator.tell(DataObfuscator.Message("123", "Text"))
        dataEnricher.tell(DataEnricher.Message("123", "Text"))
        contentValidator.tell(DataObfuscator.Message("123", "Text2"))
        dataEnricher.tell(DataEnricher.Message("123", "Text2"))

        // then one aggregator receives both while the other receives none
        probe1.receiveSeveralMessages(2)
        probe2.expectNoMessage()
    }
}