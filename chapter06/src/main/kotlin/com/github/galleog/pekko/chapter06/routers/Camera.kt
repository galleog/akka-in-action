package com.github.galleog.pekko.chapter06.routers

import org.apache.pekko.actor.typed.ActorRef
import org.apache.pekko.actor.typed.Behavior
import org.apache.pekko.actor.typed.javadsl.Behaviors
import org.apache.pekko.actor.typed.javadsl.Routers

object Camera {
    data class Photo(val content: String)

    fun create(): Behavior<Photo> = Behaviors.setup { context ->
        val routingBehavior: Behavior<String> = Routers.group(PhotoProcessor.KEY)
            .withRoundRobinRouting()
        val router: ActorRef<String> = context.spawn(routingBehavior, "photo-processor-pool")

        Behaviors.receiveMessage { message ->
            when (message) {
                is Photo -> {
                    router.tell(message.content)
                    Behaviors.same()
                }
            }
        }
    }
}