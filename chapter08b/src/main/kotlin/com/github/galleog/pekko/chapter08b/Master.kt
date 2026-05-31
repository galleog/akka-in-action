package com.github.galleog.pekko.chapter08b

import org.apache.pekko.actor.typed.ActorRef
import org.apache.pekko.actor.typed.Behavior
import org.apache.pekko.actor.typed.javadsl.Behaviors
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

object Master {
    sealed interface Event
    data object Tick : Event
    data class CountedWords(val aggregation: Map<String, Int>) : Event, CborSerializable
    data class FailedJob(val text: String) : Event

    fun create(workerRouter: ActorRef<Worker.Command>): Behavior<Event> = Behaviors.withTimers { timers ->
        timers.startTimerWithFixedDelay(Tick, Tick, 1.seconds.toJavaDuration())
        working(workerRouter)
    }

    fun working(
        workersRouter: ActorRef<Worker.Command>,
        countedWords: Map<String, Int> = emptyMap(),
        lag: List<String> = emptyList()
    ): Behavior<Event> = Behaviors.setup { context ->
        val timeout = 3.seconds.toJavaDuration()
        val paralleliism = context.system.settings().config().getInt("example.countwords.delegation-parallelism")

        Behaviors.receiveMessage { message ->
            when (message) {
                is Tick -> {
                    context.log.debug("tick, current lag {}", lag.size)

                    val text = "this simulates a stream, a very simple stream"
                    val allTexts = lag + text

                    val firstPart = allTexts.take(paralleliism)
                    for (text in firstPart) {
                        context.ask(
                            Event::class.java,
                            workersRouter,
                            timeout,
                            { replyTo -> Worker.Process(text, replyTo) },
                            { response, throwable ->
                                when {
                                    throwable != null -> FailedJob(text)
                                    response is CountedWords -> response
                                    else -> FailedJob(text)
                                }
                            }
                        )
                    }

                    val secondPart = allTexts.drop(paralleliism)
                    working(workersRouter, countedWords, secondPart)
                }

                is CountedWords -> {
                    val merged = merge(countedWords, message.aggregation)
                    context.log.debug("current count {}", merged)
                    working(workersRouter, merged, lag)
                }

                is FailedJob -> {
                    context.log.debug("failed, adding text to lag {}", lag.size)
                    working(workersRouter, countedWords, lag + message.text)
                }
            }
        }
    }

    fun merge(
        currentCount: Map<String, Int>,
        newCount2Add: Map<String, Int>
    ): Map<String, Int> =
        (currentCount.entries + newCount2Add.entries)
            .groupingBy { it.key }
            .fold(0) { acc, entry -> acc + entry.value }
}