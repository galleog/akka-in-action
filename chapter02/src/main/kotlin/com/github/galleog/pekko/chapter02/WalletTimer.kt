package com.github.galleog.pekko.chapter02

import org.apache.pekko.actor.typed.ActorSystem
import org.apache.pekko.actor.typed.Behavior
import org.apache.pekko.actor.typed.javadsl.Behaviors
import java.time.Duration

object WalletTimer {
    sealed interface Command
    data class Increase(val amount: Int) : Command
    private data object Activate : Command
    data class Deactivate(val seconds: Int) : Command

    fun create() = activated(0)

    private fun activated(total: Int): Behavior<Command> = Behaviors.receive { context, command ->
        Behaviors.withTimers { timers ->
            when (command) {
                is Increase -> {
                    val current = total + command.amount
                    context.log.info("Increasing to $current")
                    activated(current)
                }

                is Activate -> {
                    context.log.info("Wallet is activated. Can't be activated again")
                    Behaviors.same()
                }

                is Deactivate -> {
                    timers.startSingleTimer(Activate, Duration.ofSeconds(command.seconds.toLong()))
                    context.log.info("Deactivating for ${command.seconds} seconds")
                    deactivated(total)
                }
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

            is Deactivate -> {
                context.log.info("Wallet is deactivated. Can't be deactivated again")
                Behaviors.same()
            }
        }
    }
}

fun main() {
    val wallet = ActorSystem.create(WalletTimer.create(), "wallet-timer")
    wallet.tell(WalletTimer.Increase(1))
    wallet.tell(WalletTimer.Deactivate(3))
    wallet.tell(WalletTimer.Increase(1))

    println("Press ENTER to terminate")
    readlnOrNull()
    wallet.terminate()
}