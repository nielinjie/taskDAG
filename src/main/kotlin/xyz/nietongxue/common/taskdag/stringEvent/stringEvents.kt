package xyz.nietongxue.common.taskdag.stringEvent

import xyz.nietongxue.common.taskdag.Context
import xyz.nietongxue.common.taskdag.Task


object EventDefaults {
    const val START = "start"
    const val SUCCESS = "success"
    const val EXCEPTION = "exception"

}

abstract class AbstractTask : Task<String> {
    val logger = org.slf4j.LoggerFactory.getLogger(this::class.java)
    override fun exception(context: Context, e: Exception): String {
        logger.error("exception", e)
        return EventDefaults.EXCEPTION
    }
}