package bravebot

import bravebot.config.loadConfig
import bravebot.inject.BraveBotModule
import bravebot.listeners.MessageReceivedListener
import bravebot.listeners.ReadyListener
import bravebot.listeners.ShutdownListener
import com.google.inject.Guice
import sx.blah.discord.api.IDiscordClient
import java.nio.file.Paths

fun main(args: Array<String>) {
    if (args.isEmpty()) throw IllegalArgumentException("Config file required")

    val config = loadConfig(Paths.get(args[0]))

    val injector = Guice.createInjector(BraveBotModule(config))

    val client = injector.getInstance<IDiscordClient>()
    with (client.dispatcher) {
        registerListener(injector.getInstance<ReadyListener>())
        registerListener(injector.getInstance<ShutdownListener>())
        registerListener(injector.getInstance<MessageReceivedListener>())
    }
    client.login()
}
