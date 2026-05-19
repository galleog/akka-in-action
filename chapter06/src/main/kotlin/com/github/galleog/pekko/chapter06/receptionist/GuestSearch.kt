package com.github.galleog.pekko.chapter06.receptionist

import org.apache.pekko.actor.typed.ActorRef
import org.apache.pekko.actor.typed.Behavior
import org.apache.pekko.actor.typed.javadsl.AbstractBehavior
import org.apache.pekko.actor.typed.javadsl.ActorContext
import org.apache.pekko.actor.typed.javadsl.Behaviors
import org.apache.pekko.actor.typed.javadsl.Receive
import org.apache.pekko.actor.typed.receptionist.Receptionist

class GuestSearch private constructor(
    private val actorName: String,
    private val replyTo: ActorRef<ActorRef<VipGuest.Command>>,
    context: ActorContext<Command>
) : AbstractBehavior<GuestSearch.Command>(context) {
    sealed interface Command
    data object Find : Command
    private data class ListingResponse(val listing: Receptionist.Listing) : Command

    private val listingResponseAdapter: ActorRef<Receptionist.Listing> =
        context.messageAdapter(Receptionist.Listing::class.java, ::ListingResponse)

    override fun createReceive(): Receive<Command> = newReceiveBuilder()
        .onMessage(Find::class.java, ::onFind)
        .onMessage(ListingResponse::class.java, ::onListingResponse)
        .build()

    private fun onFind(message: Find): Behavior<Command> {
        context.system.receptionist().tell(Receptionist.find(HotelConcierge.GOLDEN_KEY, listingResponseAdapter))
        return this
    }

    private fun onListingResponse(message: ListingResponse): Behavior<Command> {
        message.listing.getServiceInstances(HotelConcierge.GOLDEN_KEY)
            .filter { actorName in it.path().name() }
            .forEach { replyTo.tell(it) }
        return Behaviors.stopped()
    }

    companion object {
        fun create(actorName: String, replyTo: ActorRef<ActorRef<VipGuest.Command>>) : Behavior<Command> =
            Behaviors.setup { context -> GuestSearch(actorName, replyTo, context) }
    }
}