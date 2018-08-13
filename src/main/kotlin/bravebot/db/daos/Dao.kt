package bravebot.db.daos

import bravebot.db.Db
import org.jooq.DSLContext

abstract class Dao(private val db: Db) {
    private var _dsl: DSLContext? = null
    protected val dsl: DSLContext get() = _dsl ?: throw IllegalStateException("Must be in a transaction to access DSLContext")

    fun <R> tx(block: () -> R): R {
        return db.withTx {
            _dsl = it
            val r = block()
            _dsl = null
            r
        }
    }
}
