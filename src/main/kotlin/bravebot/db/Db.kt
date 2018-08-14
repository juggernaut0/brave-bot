package bravebot.db

import org.jooq.DSLContext
import org.jooq.SQLDialect
import org.jooq.impl.DSL
import javax.sql.DataSource

class Db(private val dataSource: DataSource) {
    fun <T> withTx(block: (DSLContext) -> T): T {
        // TODO WIP
        dataSource.connection.use {
            val dsl = DSL.using(it, SQLDialect.POSTGRES)
            return dsl.transactionResult { config -> block(DSL.using(config)) }
        }
    }
}


