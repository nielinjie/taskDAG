package xyz.nietongxue.common.taskdag.stringEvent

import xyz.nietongxue.common.taskdag.*
import xyz.nietongxue.common.taskdag.stringEvent.EventDefaults.EXCEPTION

class Retry(val task: Task<String>, val times: Int = 3, val catchingEvent: String = EXCEPTION) : Task<String> {

    override val name: String = "retry_${task.name}"
    val retryCountingVariableName = "${name}_retryCountingVariable"
    val gotoRetryEvent = "goto_${task.name}_retry"
    override fun action(context: Context): ActionResult<String> {
        val count = context[retryCountingVariableName] as? Int ?: 0
        val result = task.action(context)
        val (event, newContext) = result
        return if (event == catchingEvent && count < times) {
            gotoRetryEvent to newContext.plus(retryCountingVariableName to count + 1)
        } else {
            event to newContext
        }
    }

    override fun exception(context: Context, e: Exception): String {
        return gotoRetryEvent
    }
}


fun TaskDAGBuilder<String>.retry(
    task: Task<String>,
    times: Int = 3,
    catchingEvent: String = EXCEPTION,
) {
    val retry = Retry(task, times, catchingEvent)
    // replace the original task with retry
    this.modifier(ReplaceTask(task.name, retry))
    // add retrying trans
    this.modifier(AddTrans(Trans(retry.name, retry.name, "goto_${task.name}_retry")))

}