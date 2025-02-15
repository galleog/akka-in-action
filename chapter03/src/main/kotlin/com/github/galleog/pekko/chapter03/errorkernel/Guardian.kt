package com.github.galleog.pekko.chapter03.errorkernel

import org.apache.pekko.actor.typed.ActorRef
import org.apache.pekko.actor.typed.ActorSystem
import org.apache.pekko.actor.typed.Behavior
import org.apache.pekko.actor.typed.javadsl.AbstractBehavior
import org.apache.pekko.actor.typed.javadsl.ActorContext
import org.apache.pekko.actor.typed.javadsl.Behaviors
import org.apache.pekko.actor.typed.javadsl.Receive

class Guardian private constructor(context: ActorContext<Command>) : AbstractBehavior<Guardian.Command>(context) {
    sealed interface Command
    data class Start(val text: List<String>) : Command

    private val manager: ActorRef<Manager.Command>

    init {
        context.log.info("Setting up. Creating manager")
        manager = context.spawn(Manager.create(), "manager-alpha")
    }

    override fun createReceive(): Receive<Command> =
        newReceiveBuilder().onMessage(Command::class.java, this::onMessage).build()

    private fun onMessage(command: Command): Behavior<Command> = when (command) {
        is Start -> {
            context.log.info("Delegating ${command.text} to manager")
            manager.tell(Manager.Delegate(command.text))
            this
        }
    }

    companion object {
        fun create(): Behavior<Command> = Behaviors.setup(::Guardian)
    }
}

fun main() {
    val guardian: ActorSystem<Guardian.Command> = ActorSystem.create(Guardian.create(), "guardian")
    guardian.tell(Guardian.Start(listOf("-one-", "--two--")))

    println("Press ENTER to terminate")
    readlnOrNull()
    guardian.terminate()
}