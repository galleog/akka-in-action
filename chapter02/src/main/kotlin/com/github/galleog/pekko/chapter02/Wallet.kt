package com.github.galleog.pekko.chapter02

import org.apache.pekko.actor.typed.ActorSystem
import org.apache.pekko.actor.typed.Behavior
import org.apache.pekko.actor.typed.javadsl.AbstractBehavior
import org.apache.pekko.actor.typed.javadsl.ActorContext
import org.apache.pekko.actor.typed.javadsl.Behaviors
import org.apache.pekko.actor.typed.javadsl.Receive

class Wallet private constructor(context: ActorContext<Int>) : AbstractBehavior<Int>(context) {
    override fun createReceive(): Receive<Int> =
        newReceiveBuilder().onMessage(Int::class.javaObjectType, this::onReceive).build()

    private fun onReceive(num: Int): Behavior<Int> {
        context.log.info("Received $num dollar(s)")
        return this
    }

    companion object {
        fun create(): Behavior<Int> = Behaviors.setup(::Wallet)
    }
}

fun main() {
    val wallet = ActorSystem.create(Wallet.create(), "wallet")
    wallet.tell(1)
    wallet.tell(10)

    println("Press ENTER to terminate")
    readlnOrNull()
    wallet.terminate()
}