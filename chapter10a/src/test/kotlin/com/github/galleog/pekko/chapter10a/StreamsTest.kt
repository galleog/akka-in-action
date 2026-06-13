package com.github.galleog.pekko.chapter10a

import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe
import org.apache.pekko.Done
import org.apache.pekko.NotUsed
import org.apache.pekko.actor.Cancellable
import org.apache.pekko.actor.testkit.typed.annotations.JUnit5TestKit
import org.apache.pekko.actor.testkit.typed.javadsl.ActorTestKit
import org.apache.pekko.actor.testkit.typed.javadsl.LogCapturingExtension
import org.apache.pekko.actor.testkit.typed.javadsl.TestKitJUnit5Extension
import org.apache.pekko.japi.Pair
import org.apache.pekko.stream.javadsl.*
import org.junit.jupiter.api.extension.ExtendWith
import java.util.concurrent.CompletionStage
import java.util.concurrent.TimeUnit
import kotlin.random.Random
import kotlin.test.Test
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

@ExtendWith(TestKitJUnit5Extension::class, LogCapturingExtension::class)
class StreamsTest {
    @JUnit5TestKit
    private val testKit = ActorTestKit.create()

    @Test
    fun `a finite producer should allow a consumer to receive only even numbers`() {
        val producer: Source<Int, NotUsed> = Source.from(listOf(1, 2, 3))
        val processor: Flow<Int, Int, NotUsed> = Flow.of(Int::class.java)
            .filter { it % 2 == 0 }
        val consumer: Sink<Int, CompletionStage<List<Int>>> = Sink.seq()

        val future: CompletionStage<List<Int>> = producer.via(processor)
            .toMat(consumer, Keep.right())
            .run(testKit.system())

        val list = future.toCompletableFuture().get(3, TimeUnit.SECONDS)
        list shouldBe listOf(2)
    }

    @Test
    fun `should receive only even numbers with a side effect`() {
        val fakeDB = mutableListOf<Int>()
        fun storeDB(value: Int) {
            fakeDB.add(value)
        }

        val future: CompletionStage<Done> = Source.from(listOf(1, 2, 3))
            .filter { it % 2 == 0 }
            .runForeach(::storeDB, testKit.system())

        future.toCompletableFuture().get(1, TimeUnit.SECONDS)
        fakeDB shouldBe listOf(2)
    }

    @Test
    fun `should receive only even numbers with a side effect and flow`() {
        val fakeDB = mutableListOf<Int>()
        fun storeDB(value: Int) {
            fakeDB.add(value)
        }

        val producer: Source<Int, NotUsed> = Source.from(listOf(1, 2, 3))
        val processor: Flow<Int, Int, NotUsed> = Flow.of(Int::class.java)
            .filter { it % 2 == 0 }

        val future: CompletionStage<Done> = producer.via(processor)
            .runForeach(::storeDB, testKit.system())

        future.toCompletableFuture().get(1, TimeUnit.SECONDS)
        fakeDB shouldBe listOf(2)
    }

    @Test
    fun `should receive only even numbers with a side effect and blueprint`() {
        val fakeDB = mutableListOf<Int>()
        fun storeDB(value: Int) {
            fakeDB.add(value)
        }

        val producer: Source<Int, NotUsed> = Source.from(listOf(1, 2, 3))
        val processor: Flow<Int, Int, NotUsed> = Flow.of(Int::class.java)
            .filter { it % 2 == 0 }
        val consumer: Sink<Int, CompletionStage<Done>> = Sink.foreach(::storeDB)

        val blueprint: RunnableGraph<CompletionStage<Done>> = producer
            .via(processor)
            .toMat(consumer, Keep.right())

        val future: CompletionStage<Done> = blueprint.run(testKit.system())

        future.toCompletableFuture().get(1, TimeUnit.SECONDS)
        fakeDB shouldBe listOf(2)
    }

    @Test
    fun `an infinite String producer should be cancellable`() {
        val liveSource: Source<String, Cancellable> =
            Source.tick(1.seconds.toJavaDuration(), 1.seconds.toJavaDuration(), "Hello, world")
        val masking: Flow<String, String, NotUsed> = Flow.of(String::class.java)
            .map { it.replace("World", "xyz") }

        fun dbFakeInsert(value: String) {
            println("inserting $value")
        }

        val dbFakeSink: Sink<String, CompletionStage<Done>> = Sink.foreach(::dbFakeInsert)

        val pair: Pair<Cancellable, CompletionStage<Done>> = liveSource.via(masking)
            .toMat(dbFakeSink, Keep.both())
            .run(testKit.system())

        Thread.sleep(3000)
        pair.first().cancel()

        val future = pair.second().toCompletableFuture()
        future.get(1, TimeUnit.SECONDS)
        future.isDone.shouldBeTrue()
    }

    @Test
    fun `an infinite Int producer should be cancellable`() {
        val fakeDB = mutableListOf<Int>()
        fun storeDB(value: Int) {
            fakeDB.add(value)
        }

        val liveSource: Source<Int, Cancellable> =
            Source.tick(1.seconds.toJavaDuration(), 1.seconds.toJavaDuration(), Random.nextInt())

        val pair = liveSource.filter { it % 2 == 0 }
            .toMat(Sink.foreach(::storeDB), Keep.both())
            .run(testKit.system())

        Thread.sleep(3000)
        pair.first().cancel()

        val future = pair.second().toCompletableFuture()
        future.get(1, TimeUnit.SECONDS)
        future.isDone.shouldBeTrue()
    }
}