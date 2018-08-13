package bravebot.db.daos

import bravebot.db.Db
import org.jooq.DSLContext

abstract class Dao(private val db: Db) {
    protected val dsl: DSLContext get() = threadDsl.get() ?: throw IllegalStateException("Must be in a transaction to access DSLContext")

    fun <R> tx(block: () -> R): R {
        return db.withTx {
            threadDsl.set(it)
            val r = block()
            threadDsl.remove()
            r
        }
    }

    companion object {
        private val threadDsl = ThreadLocal<DSLContext>()
    }
}
