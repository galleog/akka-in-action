package com.github.galleog.pekko.chapter04.fishing

import org.apache.pekko.actor.testkit.typed.annotations.JUnit5TestKit
import org.apache.pekko.actor.testkit.typed.javadsl.ActorTestKit
import org.apache.pekko.actor.testkit.typed.javadsl.FishingOutcomes
import org.apache.pekko.actor.testkit.typed.javadsl.LogCapturingExtension
import org.apache.pekko.actor.testkit.typed.javadsl.TestKitJUnit5Extension
import org.apache.pekko.actor.typed.ActorRef
import org.apache.pekko.actor.typed.ActorSystem
import org.apache.pekko.actor.typed.Behavior
import org.apache.pekko.actor.typed.javadsl.Behaviors
import org.apache.pekko.actor.typed.javadsl.TimerScheduler
import org.junit.jupiter.api.extension.ExtendWith
import java.time.Duration
import kotlin.random.Random
import kotlin.test.Test
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.nanoseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

@ExtendWith(TestKitJUnit5Extension::class, LogCapturingExtension::class)
class FishingTest {
    @JUnit5TestKit
    private val testKit = ActorTestKit.create()

    @Test
    fun `a timing test must be able to cancel timer`() {
        val probe = testKit.createTestProbe<Receiver.Command>()

        val interval = 100.milliseconds.toJavaDuration()
        val timerKey = "key1234"

        val sender = Behaviors.withTimers { timers ->
            timers.startTimerAtFixedRate(timerKey, Sender.Tick, interval)
            Sender.create(probe.ref, timers)
        }

        val ref = testKit.spawn(sender)
        probe.expectMessage(Receiver.Tock); probe.fishForMessage(3.seconds.toJavaDuration()) { msg ->
            when (msg) {
                // we don't know that we will see exactly one tock
                is Receiver.Tock -> {
                    if (Random.nextInt(4) == 0) ref.tell(Sender.Cancel(timerKey))
                    FishingOutcomes.continueAndIgnore()
                }

                // but we know that after we saw Cancelled we won't see any more
                is Receiver.Cancelled -> FishingOutcomes.complete()
            }
        }
        probe.expectNoMessage(interval.plus(100.milliseconds.toJavaDuration()).dilated(testKit.system()))
    }

    @Test
    fun `an automated resuming counter must receive a resume after a pause`() {
        val probe = testKit.createTestProbe<CounterTimer.Command>()
        val counterMonitored =
            Behaviors.monitor(CounterTimer.Command::class.java, probe.ref, CounterTimer.create())
        val counter = testKit.spawn(counterMonitored)

        counter.tell(CounterTimer.Pause(1))
        probe.fishForMessage(3.seconds.toJavaDuration()) { msg ->
            when (msg) {
                is CounterTimer.Increase -> FishingOutcomes.continueAndIgnore()
                is CounterTimer.Pause -> FishingOutcomes.continueAndIgnore()
                is CounterTimer.Resume -> FishingOutcomes.complete()
            }
        }
    }
}

fun Duration.dilated(system: ActorSystem<*>): Duration {
    val factor = system.settings().config().getDouble("pekko.test.timefactor")
    return (this.toNanos() * factor).nanoseconds.toJavaDuration()
}

object Receiver {
    sealed interface Command
    data object Tock : Command
    data object Cancelled : Command
}

object Sender {
    sealed interface Command
    data object Tick : Command
    data class Cancel(val key: String) : Command

    fun create(forwardTo: ActorRef<Receiver.Command>, timer: TimerScheduler<Command>): Behavior<Command> =
        Behaviors.receive { _, command ->
            when (command) {
                is Tick -> {
                    forwardTo.tell(Receiver.Tock)
                    Behaviors.same()
                }

                is Cancel -> {
                    timer.cancel(command.key)
                    forwardTo.tell(Receiver.Cancelled)
                    Behaviors.same()
                }
            }
        }
}