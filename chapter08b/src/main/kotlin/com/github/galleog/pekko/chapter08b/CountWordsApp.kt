package com.github.galleog.pekko.chapter08b

import com.typesafe.config.ConfigFactory
import org.apache.pekko.actor.typed.ActorSystem

fun main(args: Array<String>) {
    if (args.isEmpty()) {
        startup("aggregator", 0)
    } else {
        require(args.size == 2) { "Usage: two params required 'role' and 'port'" }
        startup(args[0], args[1].toInt())
    }
}

fun startup(role: String, port: Int) {
    val config = ConfigFactory.parseString(
        """
        pekko.remote.artery.canonical.port=$port
        pekko.cluster.roles = [$role]
        """.trimIndent()
    ).withFallback(ConfigFactory.load())

    ActorSystem.create(ClusteredGuardian.create(), "WordsCluster", config)
}