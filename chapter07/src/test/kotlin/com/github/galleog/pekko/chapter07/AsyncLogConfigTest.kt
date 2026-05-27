package com.github.galleog.pekko.chapter07

import com.typesafe.config.ConfigFactory
import org.apache.pekko.actor.testkit.typed.annotations.JUnit5TestKit
import org.apache.pekko.actor.testkit.typed.javadsl.ActorTestKit
import org.apache.pekko.actor.testkit.typed.javadsl.LoggingTestKit
import org.apache.pekko.actor.testkit.typed.javadsl.TestKitJUnit5Extension
import org.apache.pekko.actor.typed.Behavior
import org.apache.pekko.actor.typed.javadsl.Behaviors
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.test.Test

@ExtendWith(TestKitJUnit5Extension::class)
class AsyncLogConfigTest {
    @JUnit5TestKit
    private var testKit = ActorTestKit.create(
        ConfigFactory.parseString("""pekko.eventsourced-entity.journal-enabled = false""")
            .withFallback(ConfigFactory.load("in-memory"))
    )

    @Test
    fun `should log in debug the content when receiving message`() {
        val loggerBehavior: Behavior<String> = Behaviors.receive { context, message ->
            context.log.debug("message '$message' received")
            Behaviors.same()
        }

        val loggerActor = testKit.spawn(loggerBehavior)
        val message = "hi"

        LoggingTestKit.debug("message '$message' received")
            .expect(testKit.system()) { loggerActor.tell(message) }
    }

    @Test
    fun `should lift one property from conf`() {
        val inmemory = testKit.system().settings().config()
        val journalEnabled = inmemory.getString("pekko.eventsourced-entity.journal-enabled")
        val readJournal = inmemory.getString("pekko.eventsourced-entity.read-journal")

        val loggerBehavior: Behavior<String> = Behaviors.receive { context, _ ->
            context.log.info("$journalEnabled $readJournal")
            Behaviors.same()
        }

        val loggerActor = testKit.spawn(loggerBehavior)
        val message = "anymessage"

        LoggingTestKit.info("false inmem-read-journal")
            .expect(testKit.system()) { loggerActor.tell(message) }
    }
}