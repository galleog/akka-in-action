package com.github.galleog.pekko.chapter02

import org.apache.pekko.actor.typed.ActorSystem
import org.apache.pekko.actor.typed.Behavior
import org.apache.pekko.actor.typed.javadsl.AbstractBehavior
import org.apache.pekko.actor.typed.javadsl.ActorContext
import org.apache.pekko.actor.typed.javadsl.Behaviors
import org.apache.pekko.actor.typed.javadsl.Receive

class WalletState private constructor(val total: Int, val max: Int, context: ActorContext<Command>) :
    AbstractBehavior<WalletState.Command>(context) {
    sealed interface Command
    data class Increase(val amount: Int) : Command
    data class Decrease(val amount: Int) : Command

    override fun createReceive(): Receive<Command> =
        newReceiveBuilder().onMessage(Command::class.java, this::onReceive).build()

    private fun onReceive(command: Command): Behavior<Command> {
        return when (command) {
            is Increase -> {
                val current = total + command.amount
                if (current <= max) {
                    context.log.info("Increasing to $current")
                    WalletState(current, max, context)
                } else {
                    context.log.info("I'm overloaded. Counting '$current' while max is '$max. Stopping")
                    Behaviors.stopped()
                }
            }

            is Decrease -> {
                val current = total - command.amount
                if (current < 0) {
                    context.log.info("Can't run below zero. Stopping")
                    Behaviors.stopped()
                } else {
                    context.log.info("Decreasing to $current")
                    WalletState(current, max, context)
                }
            }
        }
    }

    companion object {
        fun create(total: Int, max: Int): Behavior<Command> = Behaviors.setup { WalletState(total, max, it) }
    }
}

fun main() {
    val wallet = ActorSystem.create(WalletState.create(0, 2), "wallet-state")
    wallet.tell(WalletState.Increase(2))
    wallet.tell(WalletState.Decrease(1))
    wallet.tell(WalletState.Increase(1))
    wallet.tell(WalletState.Increase(1))

    println("Press ENTER to terminate")
    readlnOrNull()
    wallet.terminate()
}

