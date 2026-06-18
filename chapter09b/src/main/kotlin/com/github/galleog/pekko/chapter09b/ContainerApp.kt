package com.github.galleog.pekko.chapter09b

import com.github.galleog.pekko.chapter09b.persistence.SpContainer
import org.apache.pekko.actor.typed.ActorRef
import org.apache.pekko.actor.typed.ActorSystem
import org.apache.pekko.actor.typed.javadsl.Behaviors
import org.apache.pekko.cluster.sharding.typed.ShardingEnvelope
import org.apache.pekko.cluster.sharding.typed.javadsl.ClusterSharding
import org.apache.pekko.cluster.sharding.typed.javadsl.Entity
import org.slf4j.LoggerFactory

object ContainerApp {
    private val logger = LoggerFactory.getLogger(ContainerApp::class.java)

    @JvmStatic
    fun main(args: Array<String>) {
        val system = ActorSystem.create<Void>(Behaviors.empty(), "containers")
        try {
            val shardRegion = init(system)
            commandLoop(system, shardRegion)
        } catch (e: Exception) {
            logger.error("terminating by exception", e)
            system.terminate()
        }
    }

    private fun init(system: ActorSystem<*>): ActorRef<ShardingEnvelope<SpContainer.Command>> {
        val sharding = ClusterSharding.get(system)
        val entityDef = Entity.of(SpContainer.TYPE_KEY) { context -> SpContainer.create(context.entityId) }
        return sharding.init(entityDef)
    }

    private fun commandLoop(system: ActorSystem<*>, shardRegion: ActorRef<ShardingEnvelope<SpContainer.Command>>) {
        print("Enter command: ")
        val commandLine = readlnOrNull()
        if (commandLine == null) {
            system.terminate()
        } else {
            val command = CommandLineParser.parse(commandLine)
            when (command) {
                is CommandLineParser.AddCargo -> {
                    shardRegion.tell(
                        ShardingEnvelope(
                            command.containerId,
                            SpContainer.AddCargo(
                                SpContainer.Cargo(
                                    command.cargoId,
                                    command.cargoKind,
                                    command.cargoSize
                                )
                            )
                        )
                    )

                    commandLoop(system, shardRegion)
                }

                is CommandLineParser.Unknown -> {
                    logger.warn("Unknown command {}!", command)
                    commandLoop(system, shardRegion)
                }

                is CommandLineParser.Quit -> {
                    logger.info("terminating by user signal")
                    system.terminate()
                }
            }
        }
    }
}