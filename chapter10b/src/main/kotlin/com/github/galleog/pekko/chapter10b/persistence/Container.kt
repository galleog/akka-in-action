package com.github.galleog.pekko.chapter10b.persistence

import org.apache.pekko.actor.typed.ActorRef
import org.apache.pekko.actor.typed.Behavior
import org.apache.pekko.actor.typed.SupervisorStrategy
import org.apache.pekko.cluster.sharding.typed.javadsl.EntityTypeKey
import org.apache.pekko.persistence.typed.PersistenceId
import org.apache.pekko.persistence.typed.javadsl.*
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

class Container private constructor(persistenceId: PersistenceId) :
    EventSourcedBehavior<Container.Command, Container.Event, Container.State>(
        persistenceId,
        SupervisorStrategy.restartWithBackoff(10.seconds.toJavaDuration(), 60.seconds.toJavaDuration(), 0.1)
    ) {
    data class Cargo(val id: String, val kind: String, val size: Int)

    sealed interface Command
    data class AddCargo(val cargo: Cargo) : Command, CborSerializable
    data class GetCargos(val replyTo: ActorRef<List<Cargo>>) : Command, CborSerializable

    sealed interface Event
    data class CargoAdded(val containerId: String, val cargo: Cargo) : Event, CborSerializable

    data class State(val cargos: List<Cargo> = emptyList()) : CborSerializable

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

    override fun tagsFor(event: Event): Set<String> = setOf("container-tag-" + persistenceId().entityId().toInt() % 3)

    override fun retentionCriteria(): RetentionCriteria = RetentionCriteria.snapshotEvery(100, 2)

    private fun onAddCargo(command: AddCargo): Effect<Event, State> =
        Effect().persist(CargoAdded(persistenceId().entityId(), command.cargo))

    private fun onGetCargos(command: GetCargos): Effect<Event, State> = Effect().none()
        .thenReply(command.replyTo) { it.cargos }

    private fun onCargoAdded(state: State, event: CargoAdded) = State(state.cargos + event.cargo)

    companion object {
        val TYPE_KEY = EntityTypeKey.create(Command::class.java, "container-type-key")

        fun create(containerId: String): Behavior<Command> = Container(PersistenceId.of(TYPE_KEY.name(), containerId))
    }
}