package com.github.galleog.pekko.chapter09a

import org.apache.pekko.actor.testkit.typed.annotations.JUnit5TestKit
import org.apache.pekko.actor.testkit.typed.javadsl.ActorTestKit
import org.apache.pekko.actor.testkit.typed.javadsl.LogCapturingExtension
import org.apache.pekko.actor.testkit.typed.javadsl.TestKitJUnit5Extension
import org.apache.pekko.actor.typed.ActorRef
import org.apache.pekko.cluster.sharding.typed.ShardingEnvelope
import org.apache.pekko.cluster.sharding.typed.javadsl.ClusterSharding
import org.apache.pekko.cluster.sharding.typed.javadsl.Entity
import org.apache.pekko.cluster.sharding.typed.javadsl.EntityRef
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.test.Test

@ExtendWith(TestKitJUnit5Extension::class, LogCapturingExtension::class)
class ContainerTest {
    @JUnit5TestKit
    private val testKit = ActorTestKit.create(ContainerTest::class.simpleName)

    @Test
    fun `a sharded freight entity should be able to add a cargo`() {
        val sharding = ClusterSharding.get(testKit.system())
        val entityDef = Entity.of(Container.TYPE_KEY) { context -> Container.create(context.entityId) }
        val shardRegion: ActorRef<ShardingEnvelope<Container.Command>> = sharding.init(entityDef)

        val containerId = "id-1"
        val cargo = Container.Cargo("id-c", "sack", 3)

        shardRegion.tell(ShardingEnvelope(containerId, Container.AddCargo(cargo)))

        val probe = testKit.createTestProbe<List<Container.Cargo>>()
        val container: EntityRef<Container.Command> = sharding.entityRefFor(Container.TYPE_KEY, containerId)

        container.tell(Container.GetCargos(probe.ref))
        probe.expectMessage(listOf(cargo))
    }
}