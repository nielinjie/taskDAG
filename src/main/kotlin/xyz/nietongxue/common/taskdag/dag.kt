package xyz.nietongxue.common.taskdag

class TaskDAG<E : Any>(
    val tasks: List<Task<E>> = emptyList(),
    val trans: List<Trans<E>> = emptyList()
) {
    //will be call just before start
    fun validate(): Result<Unit> {
        return runCatching {
            val init = tasks.filter { it is InitTask<*> }
            val ends = tasks.filter { it is EndTask<*> }
            require(init.size == 1) { "init task should be only one" }
            require(ends.size > 0) { "end task should be at least one" }
            val names = tasks.map { it.name }
            require(trans.all {
                it.from in names && it.to in names
            }) {
                "trans should be in tasks"
            }
        }
    }

    fun startEvent(): Result<E> {
        val init = tasks.first { it is InitTask<*> }
        return kotlin.runCatching {
            trans.filter { it.from == init.name }.let {
                require(it.size == 1) { "multi start event found - ${it.map { it.event }}" }
                it.first().event
            }
        }
    }

    fun normalTasks(): List<Task<E>> {
        return tasks.filter { it !is InitTask<*> && it !is EndTask<*> }
    }
}







