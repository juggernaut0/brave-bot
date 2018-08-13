package bravebot.db.migrate

import org.flywaydb.core.Flyway

fun main(args: Array<String>) {
    val host = System.getenv("DB_HOST") ?: "localhost"
    val port = System.getenv("DB_PORT") ?: "5432"
    val user = System.getenv("DB_USER") ?: "bravebot"
    val pass = System.getenv("DB_PASS") ?: "brave"
    val name = System.getenv("DB_NAME") ?: user

    migrate(host, port, name, user, pass)
}

fun migrate(host: String, port: String, name: String, user: String, pass: String) {
    val flyway = Flyway()
    flyway.setDataSource("jdbc:postgresql://$host:$port/$name", user, pass)
    flyway.migrate()
}
