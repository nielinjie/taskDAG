package xyz.nietongxue.common.taskdag


interface Modifier<E : Any> {
    fun modify(dag: TaskDAG<E>): TaskDAG<E>
}

class ReplaceTask<E : Any>(val oldName: String, val newTask: Task<E>) : Modifier<E> {
    override fun modify(dag: TaskDAG<E>): TaskDAG<E> {
        val newTasks = dag.tasks.map {
            if (it.name == oldName) newTask else it
        }
        val newTrans = dag.trans.map {
            val newFrom = if (it.from == oldName) newTask.name else it.from
            val newTo = if (it.to == oldName) newTask.name else it.to
            Trans(newFrom, newTo, it.event)
        }
        return TaskDAG(newTasks, newTrans)
    }
}

class AddTrans<E : Any>(val trans: Trans<E>) : Modifier<E> {
    override fun modify(dag: TaskDAG<E>): TaskDAG<E> {
        return TaskDAG(dag.tasks, listOf(trans) + dag.trans)
    }
}

const val END_EXCEPTION = "end_exception"

class DefaultExceptionTrans<E : Any>(val exceptionEndName: String, val exceptionEvent: E) : Modifier<E> {
    override fun modify(dag: TaskDAG<E>): TaskDAG<E> {
        val ends = dag.tasks.filterIsInstance<EndTask<*>>()
        val exceptionEnd = ends.firstOrNull { it.name == exceptionEndName }
        if (exceptionEnd == null) {
            val newEnd = justEnd<E>()
            val newTrans = (dag.normalTasks().map {
                Trans(it.name, newEnd.name, exceptionEvent)
            } + dag.trans).distinct()
            return TaskDAG(
                tasks = dag.tasks + newEnd, trans = newTrans
            )
        }
        return dag
    }
}


class DefaultStartNodeTrans<E : Any>(val taskName: String, val startEvent: E) : Modifier<E> {
    override fun modify(dag: TaskDAG<E>): TaskDAG<E> {
        val start = dag.tasks.filterIsInstance<InitTask<*>>().firstOrNull()
        var newStart: InitTask<E>? = null
        if (start == null) {
            newStart = justInit<E>()
        }
        val startTrans = Trans((start ?: newStart)!!.name, taskName, startEvent)
        return TaskDAG(
            tasks = (newStart?.let { dag.tasks + it } ?: dag.tasks),
            trans = (listOf(startTrans) + dag.trans).distinct()
        )
    }
}




