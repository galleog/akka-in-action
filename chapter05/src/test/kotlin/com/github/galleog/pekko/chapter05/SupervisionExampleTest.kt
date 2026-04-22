package com.github.galleog.pekko.chapter05

import com.typesafe.config.ConfigFactory
import io.mockk.mockkObject
import io.mockk.unmockkObject
import io.mockk.verify
import org.apache.pekko.actor.testkit.typed.javadsl.ActorTestKit
import org.apache.pekko.actor.testkit.typed.javadsl.LogCapturingExtension
import org.apache.pekko.actor.testkit.typed.javadsl.LoggingTestKit
import org.junit.jupiter.api.*
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.test.Test
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
@ExtendWith(LogCapturingExtension::class)
class SupervisionExampleTest {
    @Test
    @Order(1)
    fun `should log, stop and receive a PostStop signal`() {
        mockkObject(SupervisionExample)

        val behavior = testKit.spawn(SupervisionExample.create())
        LoggingTestKit.info("stopping")
            .expect(testKit.system()) { behavior.tell("stop") }

        verify(exactly = 1) { SupervisionExample.cleaning() }

        unmockkObject(SupervisionExample)
    }

    @Test
    @Order(2)
    fun `should grant and log`() {
        val behavior = testKit.spawn(SupervisionExample.create())
        LoggingTestKit.info("granted")
            .expect(testKit.system()) { behavior.tell("secret") }
    }

    @Test
    @Order(3)
    fun `should log, throw an Exception and receive a PostStop signal`() {
        mockkObject(SupervisionExample)

        val behavior = testKit.spawn(SupervisionExample.create())
        LoggingTestKit.info("recoverable")
            .expect(testKit.system()) {
                behavior.tell("recoverable")
            }

        verify(exactly = 1) { SupervisionExample.cleaning() }

        unmockkObject(SupervisionExample)
    }

    // This test is last because it will kill the Actor system
    @Test
    @Order(Int.MAX_VALUE)
    fun `should log, throw an Exception and stop the Actor system`() {
        val behavior = testKit.spawn(SupervisionExample.create())
        behavior.tell("fatal")
    }

    companion object {
        private lateinit var testKit: ActorTestKit

        @BeforeAll
        @JvmStatic
        fun setUp() {
            testKit = ActorTestKit.create(
                ConfigFactory.parseString("pekko.jvm-exit-on-fatal-error = off")
                    .withFallback(ConfigFactory.load())
            )
        }

        @AfterAll
        @JvmStatic
        fun tearDown() {
            ActorTestKit.shutdown(testKit.system(), 3.seconds.toJavaDuration(), false)
        }
    }
}