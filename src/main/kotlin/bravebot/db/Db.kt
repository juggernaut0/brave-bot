package bravebot.db

import bravebot.config.DataConfig
import org.jooq.DSLContext
import org.jooq.impl.DSL
import javax.sql.DataSource

class Db(private val config: DataConfig, private val dataSource: DataSource) {
    fun <T> withTx(block: (DSLContext) -> T): T {
        dataSource.connection.use {
            val dsl = DSL.using(it, config.sqlDialect)
            return dsl.transactionResult { config -> block(DSL.using(config)) }
        }
    }
}


