package com.github.galleog.pekko.chapter08b

import org.apache.pekko.actor.typed.Behavior
import org.apache.pekko.actor.typed.javadsl.Behaviors
import org.apache.pekko.actor.typed.javadsl.Routers
import org.apache.pekko.cluster.typed.Cluster
import org.apache.pekko.cluster.typed.SelfUp
import org.apache.pekko.cluster.typed.Subscribe

object ClusteredGuardian {
    fun create(): Behavior<SelfUp> = Behaviors.setup { context ->
        val cluster = Cluster.get(context.system)
        if (cluster.selfMember().hasRole("director")) {
            cluster.subscriptions().tell(Subscribe.create(context.self, SelfUp::class.java))
        }
        if (cluster.selfMember().hasRole("aggregator")) {
            val numberOfWorkers = context.system.settings().config().getInt("example.countwords.workers-per-node")
            for (i in 0 until numberOfWorkers) {
                context.spawn(Worker.create(), "worker-$i")
            }
        }

        Behaviors.receiveMessage {
            val router = context.spawnAnonymous(Routers.group(Worker.REGISTRATION_KEY))
            context.spawn(Master.create(router), "master")
            Behaviors.same()
        }
    }
}