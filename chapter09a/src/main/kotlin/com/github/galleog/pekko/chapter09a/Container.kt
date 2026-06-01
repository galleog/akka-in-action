package com.github.galleog.pekko.chapter09a

import org.apache.pekko.actor.typed.ActorRef
import org.apache.pekko.actor.typed.Behavior
import org.apache.pekko.actor.typed.javadsl.Behaviors
import org.apache.pekko.cluster.sharding.typed.javadsl.EntityTypeKey

object Container {
    data class Cargo(val id: String, val kind: String, val size: Int)

    sealed interface Command
    data class AddCargo(val cargo: Cargo) : Command, CborSerializable
    data class GetCargos(val replyTo: ActorRef<List<Cargo>>) : Command, CborSerializable

    val TYPE_KEY = EntityTypeKey.create(Command::class.java, "container-type-key")

    fun create(containerId: String): Behavior<Command> = ready(emptyList())

    fun ready(cargos: List<Cargo>): Behavior<Command> = Behaviors.receiveMessage { message ->
        when (message) {
            is AddCargo -> ready(cargos + message.cargo)
            is GetCargos -> {
                message.replyTo.tell(cargos)
                Behaviors.same()
            }
        }
    }
}