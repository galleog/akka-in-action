package com.github.galleog.pekko.chapter09b

object CommandLineParser {
    private val digitRegex = Regex("""\d+""")

    sealed interface Command

    data class AddCargo(
        val containerId: String,
        val cargoId: String,
        val cargoKind: String,
        val cargoSize: Int
    ) : Command

    data object Quit : Command
    data class Unknown(val consoleInput: String) : Command

    fun parse(consoleInput: String): Command {
        val tokens = consoleInput.trim().split(Regex("""\s+"""))
        return when {
            tokens.firstOrNull() in listOf("quit", "exit") -> Quit
            tokens.size == 4 && tokens[3].matches(digitRegex) ->
                AddCargo(
                    containerId = tokens[0],
                    cargoId = tokens[1],
                    cargoKind = tokens[2],
                    cargoSize = tokens[3].toInt()
                )

            else -> Unknown(consoleInput)
        }
    }
}