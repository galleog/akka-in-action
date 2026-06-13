package com.github.galleog.pekko.chapter10a

import org.apache.pekko.Done
import org.apache.pekko.NotUsed
import org.apache.pekko.actor.typed.ActorSystem
import org.apache.pekko.actor.typed.javadsl.Behaviors
import org.apache.pekko.stream.javadsl.*
import java.util.concurrent.CompletionStage

fun main() {
    val system: ActorSystem<Void> = ActorSystem.create(Behaviors.empty(), "runner")

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

    val future: CompletionStage<Done> = blueprint.run(system)

    future.toCompletableFuture().get()
    println("fakeDB: $fakeDB")
    system.terminate()
}