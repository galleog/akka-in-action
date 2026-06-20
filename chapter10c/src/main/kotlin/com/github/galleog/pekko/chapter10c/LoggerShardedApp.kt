package com.github.galleog.pekko.chapter10c

import com.typesafe.config.ConfigFactory
import org.apache.pekko.actor.typed.ActorSystem
import org.apache.pekko.actor.typed.javadsl.Behaviors
import org.apache.pekko.cluster.sharding.typed.javadsl.ShardedDaemonProcess
import org.slf4j.LoggerFactory

object LoggerShardedApp {
    private val logger = LoggerFactory.getLogger(javaClass)

    @JvmStatic
    fun main(args: Array<String>) {
        startup(args[0].toInt())
    }

    private fun startup(port: Int) {
        logger.info("Starting cluster on port {}", port)

        val config = ConfigFactory.parseString("pekko.remote.artery.canonical.port = $port")
            .withFallback(ConfigFactory.load())
        val system = ActorSystem.create<Unit>(Behaviors.empty(), "LoggerSharded", config)

        val tags = listOf("container-tag-1", "container-tag-2", "container-tag-3")

        ShardedDaemonProcess.get(system)
            .init(Unit.javaClass, "loggers", tags.size) { LoggerBehavior.create(tags[it]) }
    }
}