package bravebot.commands

import com.nhaarman.mockitokotlin2.mock
import org.junit.jupiter.api.Test

class RollCommandTest {
    private val cmd = RollCommand()

    @Test
    fun roll() {
        println(cmd.execute("d20", mock(), mock()))
    }

    @Test
    fun rollMany() {
        println(cmd.execute("5d20", mock(), mock()))
    }

    @Test
    fun rollPlus() {
        println(cmd.execute("d20 + 2d6 + 5", mock(), mock()))
    }
}