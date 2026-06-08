package com.github.galleog.pekko.chapter09b.persistence

import org.apache.pekko.actor.typed.ActorRef
import org.apache.pekko.actor.typed.Behavior
import org.apache.pekko.persistence.typed.PersistenceId
import org.apache.pekko.persistence.typed.javadsl.CommandHandler
import org.apache.pekko.persistence.typed.javadsl.Effect
import org.apache.pekko.persistence.typed.javadsl.EventHandler
import org.apache.pekko.persistence.typed.javadsl.EventSourcedBehavior

class Container private constructor(persistenceId: PersistenceId) :
    EventSourcedBehavior<Container.Command, Container.Event, Container.State>(persistenceId) {
    data class Cargo(val id: String, val kind: String, val size: Int)

    sealed interface Command
    data class AddCargo(val cargo: Cargo) : Command
    data class GetCargos(val replyTo: ActorRef<List<Cargo>>) : Command

    sealed interface Event
    data class CargoAdded(val containerId: String, val cargo: Cargo) : Event

    data class State(val cargos: List<Cargo> = emptyList())

    override fun emptyState(): State = State()

    override fun commandHandler(): CommandHandler<Command, Event, State> = newCommandHandlerBuilder()
        .forAnyState()
        .onCommand(AddCargo::class.java, ::onAddCargo)
        .onCommand(GetCargos::class.java, ::onGetCargos)
        .build()

    override fun eventHandler(): EventHandler<State, Event> = newEventHandlerBuilder()
        .forAnyState()
        .onEvent(CargoAdded::class.java, ::onCargoAdded)
        .build()

    private fun onAddCargo(command: AddCargo): Effect<Event, State> =
        Effect().persist(CargoAdded(persistenceId().entityId(), command.cargo))

    private fun onGetCargos(command: GetCargos): Effect<Event, State> = Effect()
        .none()
        .thenReply(command.replyTo) { it.cargos }

    private fun onCargoAdded(state: State, event: CargoAdded) = State(state.cargos + event.cargo)

    companion object {
        fun create(containerId: String): Behavior<Command> = Container(PersistenceId.ofUniqueId(containerId))
    }
}