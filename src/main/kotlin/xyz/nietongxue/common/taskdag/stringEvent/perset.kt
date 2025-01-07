package xyz.nietongxue.common.taskdag.stringEvent

import xyz.nietongxue.common.taskdag.DefaultExceptionTrans
import xyz.nietongxue.common.taskdag.DefaultStartNodeTrans
import xyz.nietongxue.common.taskdag.END_EXCEPTION
import xyz.nietongxue.common.taskdag.TaskDAGBuilder
import xyz.nietongxue.common.taskdag.stringEvent.EventDefaults.EXCEPTION
import xyz.nietongxue.common.taskdag.stringEvent.EventDefaults.START


fun TaskDAGBuilder<String>.defaultCatching(endTaskName: String = END_EXCEPTION) {
    this.modifier(DefaultExceptionTrans(endTaskName, EXCEPTION))
}

fun TaskDAGBuilder<String>.startFrom(taskName: String) {
    this.modifier(DefaultStartNodeTrans(taskName, START))
}