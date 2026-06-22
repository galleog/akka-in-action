package com.github.galleog.pekko.chapter10d

import com.github.galleog.pekko.chapter10d.projection.CargosPerContainerProjection
import com.github.galleog.pekko.chapter10d.repository.r2dbc.CargosPerContainerRepositoryImpl
import com.typesafe.config.ConfigFactory
import org.apache.pekko.actor.typed.ActorSystem
import org.apache.pekko.actor.typed.javadsl.Behaviors
import org.apache.pekko.cluster.sharding.typed.javadsl.ShardedDaemonProcess
import org.apache.pekko.projection.ProjectionBehavior
import org.slf4j.LoggerFactory

object ProjectionApp {
    private val logger = LoggerFactory.getLogger(javaClass)

    @JvmStatic
    fun main(args: Array<String>) {
        logger.info("Initializing system")
        val system = if (args.isEmpty()) initActorSystem(0) else initActorSystem(args[0].toInt())

        try {
            initProjection(system)
        } catch (e: Exception) {
            logger.error("Termination by exception", e)
            system.terminate()
        }
    }

    private fun initActorSystem(port: Int): ActorSystem<Unit> {
        val config = ConfigFactory.parseString(
            """
            pekko.remote.artery.canonical.port = $port
            """.trimIndent()
        ).withFallback(ConfigFactory.load())

        return ActorSystem.create(Behaviors.empty(), "containersprojection", config)
    }

    private fun initProjection(system: ActorSystem<*>) {
        val sliceRanges = CargosPerContainerProjection.sliceRanges(system, 4)

        ShardedDaemonProcess.get(system)
            .init(
                ProjectionBehavior.Command::class.java,
                "cargos-per-container-projection",
                sliceRanges.size,
                { index ->
                    ProjectionBehavior.create(
                        CargosPerContainerProjection.createProjectionFor(
                            system,
                            CargosPerContainerRepositoryImpl(),
                            sliceRanges[index]
                        )
                    )
                },
                ProjectionBehavior.stopMessage()
            )
    }
}