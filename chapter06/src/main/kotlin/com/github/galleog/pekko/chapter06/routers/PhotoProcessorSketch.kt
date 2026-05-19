package com.github.galleog.pekko.chapter06.routers

import org.apache.pekko.actor.typed.ActorRef
import org.apache.pekko.actor.typed.Behavior
import org.apache.pekko.actor.typed.javadsl.BehaviorBuilder
import org.apache.pekko.actor.typed.javadsl.Behaviors
import org.apache.pekko.actor.typed.receptionist.ServiceKey

object PhotoProcessorSketch {
    sealed interface Command
    data class File(val location: String, val camera: ActorRef<Camera.Photo>) : Command
    data object Done : Command

    val KEY = ServiceKey.create(String::class.java, "photo-processor-key")

    fun create(): Behavior<Command> = ready()

    private fun ready(): Behavior<Command> = BehaviorBuilder.create<Command>()
        .onMessage(File::class.java) {
            // processing the photo and when finish back to ready() state.
            // meanwhile to busy
            busy()
        }.build()

    private fun busy(): Behavior<Command> = Behaviors.receiveMessage { message ->
        when (message) {
            is File -> {
                // can't process the file sends back the photo to the camera
                message.camera.tell(Camera.Photo(message.location))
                Behaviors.same()
            }

            is Done -> ready()
        }
    }
}