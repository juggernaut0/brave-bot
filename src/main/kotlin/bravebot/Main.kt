package bravebot

import bravebot.commands.CompileCommand
import bravebot.commands.HelloCommand
import bravebot.commands.MuricaCommand
import sx.blah.discord.api.ClientBuilder
import java.nio.file.Files
import java.nio.file.Paths

fun main(args: Array<String>) {
    if (args.isEmpty()) throw IllegalArgumentException("Token file required")

    val token = Files.readAllBytes(Paths.get(args[0]))?.let { String(it, Charsets.UTF_8) }
    val client = ClientBuilder().withToken(token).build()
    with (client.dispatcher) {
        registerListener(ReadyListener())
        registerListener(MessageReceivedListener(listOf(
                HelloCommand(),
                CompileCommand(),
                MuricaCommand()
        )))
    }
    client.login()
}
