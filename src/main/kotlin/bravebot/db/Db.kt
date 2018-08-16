package bravebot.db

import bravebot.config.BraveBotConfig
import org.jooq.DSLContext
import org.jooq.impl.DSL
import javax.inject.Inject
import javax.inject.Singleton
import javax.sql.DataSource

@Singleton
class Db @Inject constructor(private val config: BraveBotConfig, private val dataSource: DataSource) {
    fun <T> withTx(block: (DSLContext) -> T): T {
        dataSource.connection.use {
            val dsl = DSL.using(it, config.data.sqlDialect)
            return dsl.transactionResult { config -> block(DSL.using(config)) }
        }
    }
}


