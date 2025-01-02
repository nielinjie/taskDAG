package xyz.nietongxue.common.taskdag

import org.slf4j.Logger

fun longTimeFunc(count: Int, name: String, logger: Logger? = null) {
    var c = 0
    while (c < count) {
        logger?.debug("$name - $c")
        c++
        Thread.sleep(100)
    }
}

fun <E : Any> TaskDAGBuilder<E>.mock(name: String, event: E): Task<E> {
    return task(name) {
        this.action = { context ->
            logger.debug("into mock $name")
            longTimeFunc(5, name, logger)
            event to context
        }
    }
}
