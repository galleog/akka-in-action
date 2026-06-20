package com.github.galleog.pekko.chapter10b

import com.github.galleog.pekko.chapter09b.persistence.SpContainer
import com.github.galleog.pekko.chapter09b.persistence.SpContainer.*
import com.typesafe.config.ConfigFactory
import org.apache.pekko.NotUsed
import org.apache.pekko.actor.testkit.typed.javadsl.ActorTestKit
import org.apache.pekko.actor.testkit.typed.javadsl.LogCapturingExtension
import org.apache.pekko.persistence.jdbc.query.javadsl.JdbcReadJournal
import org.apache.pekko.persistence.jdbc.testkit.javadsl.SchemaUtils
import org.apache.pekko.persistence.query.Offset
import org.apache.pekko.persistence.query.PersistenceQuery
import org.apache.pekko.stream.javadsl.Keep
import org.apache.pekko.stream.javadsl.Sink
import org.apache.pekko.stream.javadsl.Source
import org.apache.pekko.stream.testkit.TestSubscriber
import org.apache.pekko.stream.testkit.javadsl.TestSink
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.extension.ExtendWith
import org.testcontainers.postgresql.PostgreSQLContainer
import java.util.concurrent.TimeUnit
import kotlin.test.BeforeTest
import kotlin.test.Test

@ExtendWith(LogCapturingExtension::class)
class PersistenceQueryTest {
    private lateinit var readJournal: JdbcReadJournal

    @BeforeTest
    fun setUp() {
        readJournal = PersistenceQuery.get(testKit.system()).getReadJournalFor(
            JdbcReadJournal::class.java,
            JdbcReadJournal.Identifier()
        )
    }

    @Test
    fun `should retrieve the persistenceIds from db`() {
        val source: Source<String, NotUsed> = readJournal.persistenceIds()
        val consumer: Sink<String, TestSubscriber.Probe<String>> = TestSink.create(testKit.system())
        val probe: TestSubscriber.Probe<String> = source.toMat(consumer, Keep.right()).run(testKit.system())

        probe.expectSubscription()
            .request(3)
        probe.expectNextUnordered(
            "spcontainer-type-key|9",
            "spcontainer-type-key|11"
        )
    }

    @Test
    fun `should retrieve events from db`() {
        val source: Source<Event, NotUsed> = readJournal.eventsByTag("container-tag-0", Offset.noOffset())
            .map { it.event() as Event }
        val consumer: Sink<Event, TestSubscriber.Probe<Event>> = TestSink.create(testKit.system())
        val probe: TestSubscriber.Probe<Event> = source.toMat(consumer, Keep.right()).run(testKit.system())

        probe.expectSubscription()
            .request(2)
        probe.expectNextUnordered(
            CargoAdded("9", Cargo("456", "sack", 22)),
            CargoAdded("9", Cargo("459", "bigbag", 15))
        )
    }

    companion object {
        @JvmStatic
        private val postgres = PostgreSQLContainer("postgres:latest")

        private lateinit var testKit: ActorTestKit

        @JvmStatic
        @BeforeAll
        fun beforeAll() {
            postgres.start()

            testKit = ActorTestKit.create(
                PersistenceQueryTest::class.simpleName,
                ConfigFactory.parseString(
                    """
                        pekko-persistence-jdbc {
                          shared-databases {
                            slick {
                              db {
                                url = "${postgres.jdbcUrl}"
                                user = "${postgres.username}"
                                password = "${postgres.password}"
                              }
                            }
                          }
                        }
                    """.trimIndent()
                ).withFallback(ConfigFactory.load("application-test"))
            )

            val done = SchemaUtils.createIfNotExists(testKit.system())
            done.toCompletableFuture().get(3, TimeUnit.SECONDS)

            val actor1 = testKit.spawn(SpContainer.create("9"))
            actor1.tell(AddCargo(Cargo("456", "sack", 22)))
            actor1.tell(AddCargo(Cargo("459", "bigbag", 15)))

            val actor2 = testKit.spawn(SpContainer.create("11"))
            actor2.tell(AddCargo(Cargo("499", "barrel", 120)))
        }

        @JvmStatic
        @AfterAll
        fun afterAll() {
            testKit.shutdownTestKit()
            postgres.stop()
        }
    }
}