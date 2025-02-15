package com.github.galleog.pekko.chapter03.errorkernel

import org.apache.pekko.actor.typed.ActorRef
import org.apache.pekko.actor.typed.Behavior
import org.apache.pekko.actor.typed.javadsl.AbstractBehavior
import org.apache.pekko.actor.typed.javadsl.ActorContext
import org.apache.pekko.actor.typed.javadsl.Behaviors
import org.apache.pekko.actor.typed.javadsl.Receive

class Manager private constructor(context: ActorContext<Command>) : AbstractBehavior<Manager.Command>(context) {
    sealed interface Command
    data class Delegate(val text: List<String>) : Command
    private data class WorkerDoneAdapter(val response: Worker.Response) : Command

    private val adapter: ActorRef<Worker.Response> =
        context.messageAdapter(Worker.Response::class.java, ::WorkerDoneAdapter)

    override fun createReceive(): Receive<Command> =
        newReceiveBuilder().onMessage(Command::class.java, this::onMessage).build()

    private fun onMessage(command: Command): Behavior<Command> = when (command) {
        is Delegate -> {
            command.text.forEach {
                val worker = context.spawn(Worker.create(), "worker-$it")
                context.log.info("Sending '$it' to worker")
                worker.tell(Worker.Parse(adapter, it))
            }
            this
        }

        is WorkerDoneAdapter -> {
            val parsed = (command.response as Worker.Done).text
            context.log.info("Text '$parsed' has been finished")
            this
        }
    }

    companion object {
        fun create(): Behavior<Command> = Behaviors.setup(::Manager)
    }
}