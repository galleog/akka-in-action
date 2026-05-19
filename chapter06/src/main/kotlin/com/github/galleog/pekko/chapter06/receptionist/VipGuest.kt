package com.github.galleog.pekko.chapter06.receptionist

import org.apache.pekko.actor.typed.Behavior
import org.apache.pekko.actor.typed.javadsl.Behaviors
import org.apache.pekko.actor.typed.receptionist.Receptionist

object VipGuest {
    sealed interface Command
    data object EnterHotel : Command
    data object LeaveHotel : Command

    fun create(): Behavior<Command> = Behaviors.receive { context, message ->
        when (message) {
            is EnterHotel -> {
                context.system.receptionist().tell(Receptionist.register(HotelConcierge.GOLDEN_KEY, context.self))
                Behaviors.same()
            }

            is LeaveHotel -> {
                context.system.receptionist().tell(Receptionist.deregister(HotelConcierge.GOLDEN_KEY, context.self))
                Behaviors.same()
            }
        }
    }
}