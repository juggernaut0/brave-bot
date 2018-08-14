package bravebot

import bravebot.commands.CompileCommand
import bravebot.commands.HelloCommand
import bravebot.commands.MuricaCommand
import bravebot.commands.PollCommand
import bravebot.db.Db
import bravebot.db.daos.PollDao
import bravebot.listeners.MessageReceivedListener
import bravebot.listeners.ReadyListener
import bravebot.listeners.ShutdownListener
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import sx.blah.discord.api.ClientBuilder
import sx.blah.discord.api.internal.DiscordUtils
import java.nio.file.Files
import java.nio.file.Paths
import java.util.concurrent.Executors

fun main(args: Array<String>) {
    if (args.isEmpty()) throw IllegalArgumentException("Token file required")
    val token = Files.readAllBytes(Paths.get(args[0]))?.let { String(it, Charsets.UTF_8) }

    val dataSource = getDataSource()
    val db = Db(dataSource)

    val client = ClientBuilder().withToken(token).build()
    with (client.dispatcher) {
        registerListener(ReadyListener())
        registerListener(ShutdownListener(dataSource))
        registerListener(MessageReceivedListener(listOf(
                HelloCommand(),
                CompileCommand(),
                MuricaCommand(),
                PollCommand(PollDao(db), Executors.newSingleThreadScheduledExecutor(DiscordUtils.createDaemonThreadFactory()))
        )))
    }
    client.login()
}

private fun getDataSource(): HikariDataSource {
    val config = HikariConfig().apply {
        dataSourceClassName = "org.postgresql.ds.PGSimpleDataSource"

        addDataSourceProperty("user", "bravebot")
        addDataSourceProperty("password", "brave")
        addDataSourceProperty("serverName", "localhost")
        addDataSourceProperty("portNumber", "5432")
        addDataSourceProperty("databaseName", "bravebot")
    }
    return HikariDataSource(config)
}
