package com.github.galleog.pekko.chapter08a

import org.apache.pekko.actor.typed.ActorSystem
import org.apache.pekko.actor.typed.Behavior
import org.apache.pekko.actor.typed.javadsl.Behaviors
import org.apache.pekko.cluster.ClusterEvent.*
import org.apache.pekko.cluster.MemberStatus
import org.apache.pekko.cluster.typed.Cluster
import org.apache.pekko.cluster.typed.Subscribe
import org.apache.pekko.management.javadsl.PekkoManagement

object ClusterDomainEventListener {
    fun create(): Behavior<ClusterDomainEvent> = Behaviors.setup { context ->
        Cluster.get(context.system)
            .subscriptions()
            .tell(Subscribe.create(context.self, ClusterDomainEvent::class.java))

        Behaviors.receiveMessage { message ->
            when (message) {
                is MemberUp -> {
                    context.log.info("${message.member()} UP")
                    Behaviors.same()
                }

                is MemberExited -> {
                    context.log.info("${message.member()} EXITED")
                    Behaviors.same()
                }

                is MemberRemoved -> {
                    if (message.previousStatus() == MemberStatus.exiting()) {
                        context.log.info("Member ${message.member()} gracefully exited, REMOVED")
                    } else {
                        context.log.info("Member ${message.member()} downed after unreachable, REMOVED")
                    }
                    Behaviors.same()
                }

                is UnreachableMember -> {
                    context.log.info("${message.member()} UNREACHABLE")
                    Behaviors.same()
                }

                is ReachableMember -> {
                    context.log.info("${message.member()} REACHABLE")
                    Behaviors.same()
                }

                else -> {
                    context.log.info("not handling $message")
                    Behaviors.same()
                }
            }
        }
    }
}

fun main() {
    val guardian: ActorSystem<ClusterDomainEvent> = ActorSystem.create(ClusterDomainEventListener.create(), "words")
    PekkoManagement.get(guardian).start()
}