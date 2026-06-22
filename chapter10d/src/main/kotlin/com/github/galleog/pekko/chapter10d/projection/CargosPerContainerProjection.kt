package com.github.galleog.pekko.chapter10d.projection

import com.github.galleog.pekko.chapter09b.persistence.SpContainer
import com.github.galleog.pekko.chapter09b.persistence.SpContainer.Event
import com.github.galleog.pekko.chapter10d.repository.r2dbc.CargosPerContainerRepository
import org.apache.pekko.actor.typed.ActorSystem
import org.apache.pekko.persistence.query.Offset
import org.apache.pekko.persistence.query.typed.EventEnvelope
import org.apache.pekko.persistence.r2dbc.query.javadsl.R2dbcReadJournal
import org.apache.pekko.projection.ProjectionId
import org.apache.pekko.projection.eventsourced.javadsl.EventSourcedProvider
import org.apache.pekko.projection.javadsl.ExactlyOnceProjection
import org.apache.pekko.projection.javadsl.SourceProvider
import org.apache.pekko.projection.r2dbc.javadsl.R2dbcProjection
import java.util.*

object CargosPerContainerProjection {
    fun createProjectionFor(
        system: ActorSystem<*>,
        repository: CargosPerContainerRepository,
        sliceRange: org.apache.pekko.japi.Pair<Int, Int>
    ): ExactlyOnceProjection<Offset, EventEnvelope<Event>> {
        val minSlice = sliceRange.first()
        val maxSlice = sliceRange.second()

        val sourceProvider: SourceProvider<Offset, EventEnvelope<Event>> = EventSourcedProvider.eventsBySlices<Event>(
            system,
            R2dbcReadJournal.Identifier(),
            SpContainer.TYPE_KEY.name(),
            minSlice,
            maxSlice
        )

        return R2dbcProjection.exactlyOnce(
            ProjectionId.of("CargosPerContainerProjection", "$minSlice-$maxSlice"),
            Optional.empty(),
            sourceProvider,
            { CpcProjectionHandler(repository) },
            system
        )
    }

    fun sliceRanges(system: ActorSystem<*>, numberOfRanges: Int): List<org.apache.pekko.japi.Pair<Int, Int>> =
        EventSourcedProvider.sliceRanges(system, R2dbcReadJournal.Identifier(), numberOfRanges)
}