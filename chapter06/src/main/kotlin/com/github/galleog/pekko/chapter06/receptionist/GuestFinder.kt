package com.github.galleog.pekko.chapter06.receptionist

import org.apache.pekko.actor.typed.ActorRef
import org.apache.pekko.actor.typed.Behavior
import org.apache.pekko.actor.typed.javadsl.Behaviors
import org.apache.pekko.actor.typed.receptionist.Receptionist
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

object GuestFinder {
    sealed interface Command
    data class Find(val actorName: String, val replyTo: ActorRef<ActorRef<VipGuest.Command>>) : Command
    data object Void : Command

    fun create(): Behavior<Command> = Behaviors.setup { context ->
        Behaviors.receiveMessage { message ->
            val timeout = 3.seconds.toJavaDuration()

            when (message) {
                is Find -> {
                    context.ask(
                        Receptionist.Listing::class.java,
                        context.system.receptionist(),
                        timeout,
                        { ref -> Receptionist.find(HotelConcierge.GOLDEN_KEY, ref) },
                        { listing, e ->
                            if (e == null) {
                                for (actor in listing.getServiceInstances(HotelConcierge.GOLDEN_KEY)) {
                                    if (message.actorName in actor.path().name()) message.replyTo.tell(actor)
                                }
                            } else {
                                context.log.error(e.message)
                            }
                            Void
                        }
                    )
                    Behaviors.same()
                }

                is Void -> Behaviors.empty()
            }
        }
    }
}