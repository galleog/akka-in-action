package com.github.galleog.pekko.chapter05

import org.apache.pekko.actor.testkit.typed.annotations.JUnit5TestKit
import org.apache.pekko.actor.testkit.typed.javadsl.ActorTestKit
import org.apache.pekko.actor.testkit.typed.javadsl.LogCapturingExtension
import org.apache.pekko.actor.testkit.typed.javadsl.LoggingTestKit
import org.apache.pekko.actor.testkit.typed.javadsl.TestKitJUnit5Extension
import org.apache.pekko.actor.typed.SupervisorStrategy
import org.apache.pekko.actor.typed.javadsl.Behaviors
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.test.Test

@ExtendWith(TestKitJUnit5Extension::class, LogCapturingExtension::class)
class MonitoringExampleTest {
    @JUnit5TestKit
    private val testKit = ActorTestKit.create()

    @Test
    fun `among two actors, NO parent-child related, the watcher  must be notified with Terminated when watched actor stops`() {
        val watcher = testKit.spawn(SimplifiedFileWatcher.create())
        val logProcessor = testKit.spawn(Behaviors.receive<String> { _, msg ->
            if (msg == "stop") Behaviors.stopped() else Behaviors.same()
        })

        watcher.tell(SimplifiedFileWatcher.Watch(logProcessor))
        LoggingTestKit.info("terminated")
            .expect(testKit.system()) { logProcessor.tell("stop") }
    }

    @Test
    fun `among two actors, parent-child related, the watcher must be notified by ChildFailed if it is a child that failed`() {
        val probe = testKit.createTestProbe<String>()
        val watcher = testKit.spawn(ParentWatcher.create(probe.ref))
        watcher.tell(ParentWatcher.Spawn(ParentWatcher.childBehavior))

        watcher.tell(ParentWatcher.FailChildren)
        probe.expectMessage("childFailed")
    }

    @Test
    fun `among two actors, parent-child related, the watcher must be notified by Termination if it is a child that only stopped`() {
        val probe = testKit.createTestProbe<String>()
        val watcher = testKit.spawn(ParentWatcher.create(probe.ref))
        watcher.tell(ParentWatcher.Spawn(ParentWatcher.childBehavior))

        watcher.tell(ParentWatcher.StopChildren)
        probe.expectMessage("terminated")
    }

    @Test
    fun `among two actors, parent-child related, the watcher must not be notified if the watched child throws an Non-Fatal Exception while having a restart strategy`() {
        val restartingChildBehavior = Behaviors.supervise(ParentWatcher.childBehavior)
            .onFailure(SupervisorStrategy.restart())

        val probe = testKit.createTestProbe<String>()
        val watcher = testKit.spawn(ParentWatcher.create(probe.ref))
        watcher.tell(ParentWatcher.Spawn(restartingChildBehavior))

        watcher.tell(ParentWatcher.FailChildren)
        probe.expectNoMessage()
    }

    @Test
    fun `among two actors, parent-child related, the watcher must be notified if child with restart strategy gets stopped`() {
        val restartingChildBehavior = Behaviors.supervise(ParentWatcher.childBehavior)
            .onFailure(SupervisorStrategy.restart())

        val probe = testKit.createTestProbe<String>()
        val watcher = testKit.spawn(ParentWatcher.create(probe.ref))
        watcher.tell(ParentWatcher.Spawn(restartingChildBehavior))

        watcher.tell(ParentWatcher.StopChildren)
        probe.expectMessage("terminated")
    }
}