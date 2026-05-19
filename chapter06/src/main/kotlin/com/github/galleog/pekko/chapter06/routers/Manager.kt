package com.github.galleog.pekko.chapter06.routers

import org.apache.pekko.actor.typed.ActorRef
import org.apache.pekko.actor.typed.Behavior
import org.apache.pekko.actor.typed.javadsl.Behaviors
import org.apache.pekko.actor.typed.javadsl.Routers

object Manager {
    fun create(behavior: Behavior<String>): Behavior<Unit> = Behaviors.setup { context ->
        val routingBehavior: Behavior<String> = Routers.pool(4, behavior)
        val router: ActorRef<String> = context.spawn(routingBehavior, "test-pool")

        (0..10).forEach {
            router.tell("hi")
        }

        Behaviors.empty()
    }
}