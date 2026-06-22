package com.github.galleog.pekko.chapter10d.projection

import com.github.galleog.pekko.chapter09b.persistence.SpContainer.CargoAdded
import com.github.galleog.pekko.chapter09b.persistence.SpContainer.Event
import com.github.galleog.pekko.chapter10d.repository.r2dbc.CargosPerContainerRepository
import org.apache.pekko.Done
import org.apache.pekko.persistence.query.typed.EventEnvelope
import org.apache.pekko.projection.r2dbc.javadsl.R2dbcHandler
import org.apache.pekko.projection.r2dbc.javadsl.R2dbcSession
import org.slf4j.LoggerFactory
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletionStage

class CpcProjectionHandler(private val repository: CargosPerContainerRepository) :
    R2dbcHandler<EventEnvelope<Event>>() {
    private val logger = LoggerFactory.getLogger(javaClass)

    override fun process(
        session: R2dbcSession,
        envelope: EventEnvelope<Event>
    ): CompletionStage<Done> = when (val event = envelope.event()) {
        is CargoAdded -> repository.addCargo(event.containerId, session)
        else -> {
            logger.debug("Ignoring event {} in projection", event)
            CompletableFuture.completedFuture(Done.done())
        }
    }
}