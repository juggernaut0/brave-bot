package bravebot.commands

import sx.blah.discord.handle.obj.IChannel
import sx.blah.discord.handle.obj.IUser
import java.security.SecureRandom

class RollCommand : BaseCommand("roll") {
    override val helpText: String = makeHelpText("Roll some dice")

    private val random = SecureRandom()

    override fun execute(args: String, author: IUser, channel: IChannel): String {
        val rolls = parseRolls(args) ?: return "I didn't understand how to roll that."
        val (total, details) = executeRolls(rolls)
        return buildString {
            val rollStr = rolls.joinToString(separator = " + ")
            appendln("Rolling $rollStr...")
            for ((roll, results) in details) {
                appendln("$roll - $results")
            }
            appendln("Total: $total")
        }
    }

    // TODO negatives
    private fun parseRolls(argStr: String): List<Roll>? {
        return argStr
                .split('+')
                .map { rollStr ->
                    if ("d" in rollStr) {
                        val parts = rollStr.trim().split('d')
                        if (parts.size > 2) return null
                        val (n, d) = parts
                        val num = if (n.isBlank()) {
                            1
                        } else {
                            n.toIntOrNull() ?: return null
                        }
                        val die = d.toIntOrNull() ?: return null
                        Roll(num, die)
                    } else {
                        val num = rollStr.trim().toIntOrNull() ?: return null
                        Roll(num, 1)
                    }
                }
    }

    private fun executeRolls(rolls: List<Roll>): RollResult {
        var total = 0
        val rollResults = rolls.map {
            val nums = it.execute(random)
            total += nums.sum()
            it.toString() to nums.joinToString(prefix = "[", postfix = "]")
        }
        return RollResult(total, rollResults)
    }

    private data class Roll(val num: Int, val die: Int, val negative: Boolean = false) {
        fun execute(random: SecureRandom): List<Int> {
            return if (die == 1) listOf(num) else (1..num).map { random.nextInt(die) + 1 }
        }

        override fun toString(): String {
            return when {
                die == 1 -> num.toString()
                num == 1 -> "d$die"
                else -> "${num}d$die"
            }
        }
    }

    private data class RollResult(val total: Int, val rolls: List<Pair<String, String>>)
}