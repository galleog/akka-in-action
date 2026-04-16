package com.github.galleog.pekko.chapter04.simplified

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import org.apache.pekko.actor.testkit.typed.CapturedLogEvent
import org.apache.pekko.actor.testkit.typed.Effect
import org.apache.pekko.actor.testkit.typed.javadsl.BehaviorTestKit
import org.apache.pekko.actor.testkit.typed.javadsl.TestInbox
import org.slf4j.event.Level
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

class SyncTest {
    private lateinit var testKit: BehaviorTestKit<SimplifiedManager.Command>

    @BeforeTest
    fun setUp() {
        testKit = BehaviorTestKit.create(SimplifiedManager.create())
    }

    @Test
    fun `spawning takes place`() {
        testKit.run(SimplifiedManager.CreateChild("adan"))
        testKit.expectEffectClass(Effect.Spawned::class.java).childName() shouldBe "adan"
    }

    @Test
    fun `actor gets forwarded message from manager`() {
        val inbox = TestInbox.create<String>()
        testKit.run(SimplifiedManager.Forward("message-to-parse", inbox.ref))
        inbox.expectMessage("message-to-parse")
        inbox.hasMessages().shouldBeFalse()
    }

    @Test
    fun `record the log`() {
        testKit.run(SimplifiedManager.Log)
        testKit.allLogEntries.shouldContainExactly(CapturedLogEvent(Level.INFO, "it's done"))
    }

    @Test
    fun `failing to schedule a message because BehaviorTestKit can't deal with scheduling`() {
        testKit.run(SimplifiedManager.ScheduleLog)
        with(testKit.expectEffectClass(Effect.Scheduled::class.java)) {
            duration() shouldBe 1.seconds.toJavaDuration()
            target() shouldBe testKit.ref
            message() shouldBe SimplifiedManager.Log
        }

        shouldThrow<AssertionError> {
            testKit.allLogEntries.shouldContainExactly(CapturedLogEvent(Level.INFO, "it's done"))
        }
    }
}