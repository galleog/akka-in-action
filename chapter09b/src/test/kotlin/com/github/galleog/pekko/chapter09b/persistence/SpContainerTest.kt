package com.github.galleog.pekko.chapter09b.persistence

import com.typesafe.config.ConfigFactory
import org.apache.pekko.actor.testkit.typed.annotations.JUnit5TestKit
import org.apache.pekko.actor.testkit.typed.javadsl.ActorTestKit
import org.apache.pekko.actor.testkit.typed.javadsl.LogCapturingExtension
import org.apache.pekko.actor.testkit.typed.javadsl.TestKitJUnit5Extension
import org.apache.pekko.actor.typed.ActorRef
import org.apache.pekko.cluster.sharding.typed.ShardingEnvelope
import org.apache.pekko.cluster.sharding.typed.javadsl.ClusterSharding
import org.apache.pekko.cluster.sharding.typed.javadsl.Entity
import org.apache.pekko.cluster.sharding.typed.javadsl.EntityRef
import org.apache.pekko.persistence.testkit.javadsl.EventSourcedBehaviorTestKit
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.test.Test

@ExtendWith(TestKitJUnit5Extension::class, LogCapturingExtension::class)
class SpContainerTest {
    @JUnit5TestKit
    private val testKit = ActorTestKit.create(
        this::class.simpleName,
        EventSourcedBehaviorTestKit.config()
            .withFallback(ConfigFactory.load("application-test"))
    )

    @Test
    fun `a persistent entity with sharding should be able to add a cargo`() {
        val sharding = ClusterSharding.get(testKit.system())
        val entityDef = Entity.of(SpContainer.TYPE_KEY) { context -> SpContainer.create(context.entityId) }
        val shardRegion: ActorRef<ShardingEnvelope<SpContainer.Command>> = sharding.init(entityDef)

        val containerId = "123"
        val cargo = SpContainer.Cargo("id-c", "sack", 3)

        shardRegion.tell(ShardingEnvelope(containerId, SpContainer.AddCargo(cargo)))

        val probe = testKit.createTestProbe<List<SpContainer.Cargo>>()
        val container: EntityRef<SpContainer.Command> = sharding.entityRefFor(SpContainer.TYPE_KEY, containerId)

        container.tell(SpContainer.GetCargos(probe.ref))
        probe.expectMessage(listOf(cargo))
    }
}