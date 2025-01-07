package xyz.nietongxue.common.taskdag

import xyz.nietongxue.common.taskdag.EventDefaults.EXCEPTION

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


class DefaultStartNodeConfig<E : Any> {
    fun config(taskDAG: TaskDAG<E>): TaskDAG<E> {
        val init = taskDAG.tasks.firstOrNull() { it is InitTask }
        if (init == null) {
            val newInit = justInit<E>()
            return TaskDAG(
                tasks = listOf(newInit) + taskDAG.tasks, trans = taskDAG.trans
            )
        } else
            return taskDAG
    }
}

class DefaultEndNodeConfig<E : Any> {
    fun config(taskDAG: TaskDAG<E>): TaskDAG<E> {
        val ends = taskDAG.tasks.filter { it is EndTask }
        if (ends.isEmpty()) {
            val newEnd = justEnd<E>()
            return TaskDAG(
                tasks = taskDAG.tasks + listOf(newEnd), trans = taskDAG.trans
            )
        } else {
            return taskDAG
        }
    }
}


