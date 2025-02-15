package com.github.galleog.pekko.chapter02

import org.apache.pekko.actor.typed.ActorSystem
import org.apache.pekko.actor.typed.Behavior
import org.apache.pekko.actor.typed.javadsl.Behaviors

object WalletOnOff {
    sealed interface Command
    data class Increase(val amount: Int) : Command
    data object Activate : Command
    data object Deactivate : Command

    fun create() = activated(0)

    private fun activated(total: Int): Behavior<Command> = Behaviors.receive { context, command ->
        when (command) {
            is Increase -> {
                val current = total + command.amount
                context.log.info("Increasing to $current")
                activated(current)
            }

            is Activate -> Behaviors.same()

            is Deactivate -> {
                context.log.info("Deactivating")
                deactivated(total)
            }
        }
    }

    private fun deactivated(total: Int): Behavior<Command> = Behaviors.receive { context, command ->
        when (command) {
            is Increase -> {
                context.log.info("Wallet is deactivated. Can't increase")
                Behaviors.same()
            }

            is Activate -> {
                context.log.info("Activating")
                activated(total)
            }

            is Deactivate -> Behaviors.same()
        }
    }
}

fun main() {
    val wallet = ActorSystem.create(WalletOnOff.create(), "wallet-on-off")
    wallet.tell(WalletOnOff.Increase(1))
    wallet.tell(WalletOnOff.Deactivate)
    wallet.tell(WalletOnOff.Increase(1))
    wallet.tell(WalletOnOff.Activate)
    wallet.tell(WalletOnOff.Increase(1))

    println("Press ENTER to terminate")
    readlnOrNull()
    wallet.terminate()
}