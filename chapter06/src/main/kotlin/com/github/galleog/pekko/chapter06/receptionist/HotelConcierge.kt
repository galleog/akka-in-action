package com.github.galleog.pekko.chapter06.receptionist

import org.apache.pekko.actor.typed.ActorRef
import org.apache.pekko.actor.typed.Behavior
import org.apache.pekko.actor.typed.javadsl.AbstractBehavior
import org.apache.pekko.actor.typed.javadsl.ActorContext
import org.apache.pekko.actor.typed.javadsl.Behaviors
import org.apache.pekko.actor.typed.javadsl.Receive
import org.apache.pekko.actor.typed.receptionist.Receptionist
import org.apache.pekko.actor.typed.receptionist.ServiceKey

class HotelConcierge private constructor(context: ActorContext<Command>) :
    AbstractBehavior<HotelConcierge.Command>(context) {
    sealed interface Command
    private data class ListingResponse(val listing: Receptionist.Listing) : Command

    private val listingNotificationAdapter: ActorRef<Receptionist.Listing> =
        context.messageAdapter(Receptionist.Listing::class.java, ::ListingResponse)

    init {
        context.system.receptionist().tell(Receptionist.subscribe(GOLDEN_KEY, listingNotificationAdapter))
    }

    override fun createReceive(): Receive<Command> = newReceiveBuilder()
        .onMessage(ListingResponse::class.java, ::onMessage)
        .build()

    private fun onMessage(message: ListingResponse): Behavior<Command> {
        for (actor in message.listing.getServiceInstances(GOLDEN_KEY)) {
            context.log.info("${actor.path().name()} is in")
        }
        return this
    }

    companion object {
        val GOLDEN_KEY = ServiceKey.create(VipGuest.Command::class.java, "concierge-key")

        fun create(): Behavior<Command> = Behaviors.setup(::HotelConcierge)
    }
}