package com.github.galleog.pekko.chapter08b

import com.typesafe.config.ConfigFactory
import org.apache.pekko.actor.testkit.typed.annotations.JUnit5TestKit
import org.apache.pekko.actor.testkit.typed.javadsl.ActorTestKit
import org.apache.pekko.actor.testkit.typed.javadsl.LogCapturingExtension
import org.apache.pekko.actor.testkit.typed.javadsl.TestKitJUnit5Extension
import org.apache.pekko.actor.typed.javadsl.Behaviors
import org.apache.pekko.actor.typed.javadsl.Routers
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.test.Test

@ExtendWith(TestKitJUnit5Extension::class, LogCapturingExtension::class)
class MasterWorkersTest {
    @JUnit5TestKit
    private val testKit = ActorTestKit.create(
        ConfigFactory.parseString("example.countwords.workers-per-node = 5")
            .withFallback(ConfigFactory.load())
    )

    @Test
    fun `should send work from the master to the workers and back`() {
        // emulating guardian
        val numberOfWorkers = testKit.system().settings().config().getInt("example.countwords.workers-per-node")

        for (i in 0 until numberOfWorkers) {
            testKit.spawn(Worker.create(), "worker-$i")
        }

        val router = testKit.spawn(Routers.group(Worker.REGISTRATION_KEY))
        val probe = testKit.createTestProbe<Master.Event>()
        val masterMonitored = Behaviors.monitor(Master.Event::class.java, probe.ref, Master.create(router))
        testKit.spawn(masterMonitored, "master0")

        val expectedCountedWords = Master.CountedWords(
            mapOf(
                "this" to 1,
                "simulates" to 1,
                "a" to 2,
                "stream" to 2,
                "very" to 1,
                "simple" to 1
            )
        )
        probe.expectMessage(Master.Tick)
        probe.expectMessage(expectedCountedWords)
        probe.expectMessage(Master.Tick)
        probe.expectMessage(expectedCountedWords)
    }
}