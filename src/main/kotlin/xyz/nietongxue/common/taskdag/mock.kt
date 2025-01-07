package xyz.nietongxue.common.taskdag

import org.slf4j.Logger
import org.slf4j.LoggerFactory


fun longTimeFunc(seconds: Int, name: String, logger: Logger? = null) {
    var c = 0
    while (c < seconds) {
        logger?.debug("$name - $c")
        c++
        Thread.sleep(1000)
    }
}


class MockTask<E : Any>(
    override val name: String,
    val events: List<E>,
    val longTimeSeconds: Int = 3,
) : Task<E> {
    private val logger = LoggerFactory.getLogger(MockTask::class.java)
    private val countingVariableName = "${name}_countingVariable"
    override fun action(context: Context): ActionResult<E> {
        longTimeFunc(longTimeSeconds, name, logger)
        val count = context[countingVariableName] as? Int ?: 0
        val event = events[count]
        val newCount = count + 1
        return event to context.plus(countingVariableName to newCount)
    }

    override fun exception(context: Context, e: Exception): E {
        error("MockTask $name exception")
    }
}

fun <E : Any> TaskDAGBuilder<E>.mock(
    name: String,
    event: E,
    vararg events: E,
    longTimeSeconds: Int = 3,
): Task<E> {
    val task = MockTask(name, listOf(event) + events.toList(), longTimeSeconds)
    return this.task(task)
}