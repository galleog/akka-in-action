package com.github.galleog.pekko.chapter06.routers

import org.apache.pekko.actor.typed.ActorRef
import org.apache.pekko.actor.typed.Behavior
import org.apache.pekko.actor.typed.javadsl.AbstractBehavior
import org.apache.pekko.actor.typed.javadsl.ActorContext
import org.apache.pekko.actor.typed.javadsl.Behaviors
import org.apache.pekko.actor.typed.javadsl.Receive

class Worker private constructor(private val monitor: ActorRef<String>, context: ActorContext<String>) :
    AbstractBehavior<String>(context) {
    override fun createReceive(): Receive<String> = newReceiveBuilder()
        .onMessage(String::class.java, ::onMessage)
        .build()

    private fun onMessage(message: String): Behavior<String> {
        monitor.tell(message)
        return this
    }

    companion object {
        fun create(monitor: ActorRef<String>): Behavior<String> = Behaviors.setup { context -> Worker(monitor, context) }
    }
}