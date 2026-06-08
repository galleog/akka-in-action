package com.github.galleog.pekko.chapter09b

import io.kotest.matchers.shouldBe
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

class CommandLineParserTest {
    @ParameterizedTest
    @MethodSource("testData")
    fun `should parse command line`(s: String, command: CommandLineParser.Command) {
        CommandLineParser.parse(s) shouldBe command
    }

    companion object {
        @JvmStatic
        fun testData() = Stream.of(
            Arguments.of("quit", CommandLineParser.Quit),
            Arguments.of("exit", CommandLineParser.Quit),
            Arguments.of("a b c 1", CommandLineParser.AddCargo("a", "b", "c", 1)),
            Arguments.of("a b c d", CommandLineParser.Unknown("a b c d")),
            Arguments.of("a b c ", CommandLineParser.Unknown("a b c ")),
        )
    }
}