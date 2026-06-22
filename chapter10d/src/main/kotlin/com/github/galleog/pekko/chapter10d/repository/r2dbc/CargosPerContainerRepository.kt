package com.github.galleog.pekko.chapter10d.repository.r2dbc

import org.apache.pekko.Done
import org.apache.pekko.projection.r2dbc.javadsl.R2dbcSession
import java.util.concurrent.CompletionStage

interface CargosPerContainerRepository {
    fun addCargo(containerId: String, session: R2dbcSession): CompletionStage<Done>
}

class CargosPerContainerRepositoryImpl : CargosPerContainerRepository {
    override fun addCargo(containerId: String, session: R2dbcSession): CompletionStage<Done> {
        val statement = session.createStatement(
            """
            INSERT INTO cargos_per_container (container_id, cargos) VALUES ($1, 1)
            ON CONFLICT (container_id) DO
                UPDATE SET cargos = cargos_per_container.cargos + 1;
            """.trimIndent()
        ).bind(0, containerId)

        return session.updateOne(statement)
            .thenApply { Done.done() }
    }
}