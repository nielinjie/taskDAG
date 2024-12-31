package xyz.nietongxue.common.taskdag

class TaskDAGBuilder<E : Any>() {
    val tasks = mutableListOf<Task<E>>()
    val trans = mutableListOf<Trans<E>>()
    fun task(name: String, block: TaskBuilder<E>.() -> Unit): Task<E> {
        val builder = TaskBuilder<E>(name)
        builder.block()
        return builder.build().also {
            tasks.add(it)
        }
    }

    fun action(name: String, action: (Context) -> ActionResult<E>): Task<E> {
        return task(name) {
            this.action = action
        }
    }

    fun dag(name: String, dag: NestedDAG<E>): Task<E> {
        return task(name) {
            this.dag = dag
        }
    }

    fun pure(name: String, action: (Context) -> E): Task<E> {
        return task(name) {
            this.action = { context ->
                action(context) to context
            }
        }
    }
    fun fire(name: String, event: E): Task<E> {
        return task(name) {
            this.outEvent = event
        }
    }

    fun init(): InitTask<E> {
        return justInit<E>().also {
            tasks.add(it)
        }
    }

    fun end(): EndTask<E> {
        return justEnd<E>().also {
            tasks.add(it)
        }
    }

    fun end(name: String): EndTask<E> {
        return end(name).also {
            tasks.add(it)
        }
    }

    fun trans(from: String, to: String, event: E) {
        this.trans.add(
            Trans(from, to, event)
        )
    }

    fun Task<E>.to(name: String): TransBuilder<E> {
        return TransBuilder<E>().also {
            it.from = this.name
            it.to = name
        }
    }

    fun Task<E>.on(event: E): TransBuilder2<E> {
        return TransBuilder2<E>().also {
            it.from = this.name
            it.event = event
        }
    }

    fun String.to(name: String): TransBuilder<E> {
        return TransBuilder<E>().also {
            it.from = this
            it.to = name
        }
    }

    fun String.on(event: E): TransBuilder2<E> {
        return TransBuilder2<E>().also {
            it.from = this
            it.event = event
        }
    }

    fun String.to(task: Task<E>): TransBuilder<E> {
        ensureTask(task)
        return TransBuilder<E>().also {
            it.from = this
            it.to = task.name
        }
    }

    fun Task<E>.to(task: Task<E>): TransBuilder<E> {
        ensureTask(task)
        return TransBuilder<E>().also {
            it.from = this.name
            it.to = task.name
        }
    }

    fun ensureTask(task: Task<E>): Task<E> {
        if (this.tasks.contains(task)) {
            return task
        } else {
            this.tasks.add(task)
            return task
        }
    }

    fun TransBuilder<E>.on(event: E): Trans<E> {
        this.event = event
        return this.build().also {
            this@TaskDAGBuilder.trans.add(
                it
            )
        }
    }

    fun TransBuilder2<E>.to(name: String): Trans<E> {
        this.to = name
        return this.build().also {
            this@TaskDAGBuilder.trans.add(
                it
            )
        }
    }

    fun TransBuilder2<E>.to(task: Task<E>): Trans<E> {
        ensureTask(task)
        this.to = task.name
        return this.build().also {
            this@TaskDAGBuilder.trans.add(it)
        }
    }
}

fun <E : Any> justInit(): InitTask<E> {
    return object : InitTask<E>() {
        override val name: String = "INIT"
    }
}

fun <E : Any> justEnd(): EndTask<E> {
    return object : EndTask<E>() {
        override val name: String = "END"
    }
}

fun <E : Any> end(name: String): EndTask<E> {
    return object : EndTask<E>() {
        override val name: String = name
    }
}


typealias NestedDAG<E> = Pair<TaskDAG<E>, E>

class TaskBuilder<E : Any>(val _name: String?) {
    var name = _name
    var action: ((Context) -> ActionResult<E>)? = null
    var dag: NestedDAG<E>? = null
    var exception: ((Context, Exception) -> E)? = null
    var outEvent: E? = null
    var meta = mutableMapOf<String, Any>()
    fun build(): Task<E> {
        if (name == null) {
            throw IllegalArgumentException("name should not be null")
        }
        if (dag != null && action != null) {
            error("should not have dag and action at the same time")
        } else if (dag == null && action == null) {
            if (outEvent != null) {
                this.action = { context ->
                    outEvent!! to context
                }
            } else {
                throw IllegalArgumentException("should have action or dag")
            }
        } else if (dag != null && action == null) {
            this.action = { context ->
                val runtime = TasksRuntime(dag!!.first!!)
                runtime.start(dag!!.first.startEvent())
                runtime.waitForEnd()
                dag!!.second to context
            }
            meta["dag"] = dag!!
        }
        return object : RichTask<E>(this@TaskBuilder.meta) {
            override val name: String = this@TaskBuilder.name!!
            override fun action(context: Context): ActionResult<E> {
                return this@TaskBuilder.action!!.invoke(context)
            }


            override fun exception(context: Context, e: Exception): E {
                return this@TaskBuilder.exception?.invoke(context, e) ?: throw e
            }
        }

//        error("should not be here")
    }
}


class TransBuilder<E : Any>() {
    lateinit var from: String
    lateinit var to: String
    lateinit var event: E
    fun build(): Trans<E> {
        return Trans(from, to, event)
    }

}

class TransBuilder2<E : Any>() {
    lateinit var from: String
    lateinit var to: String
    lateinit var event: E
    fun build(): Trans<E> {
        return Trans(from, to, event)
    }
}

fun <E : Any> dag(block: TaskDAGBuilder<E>.() -> Unit): TaskDAG<E> {
    val builder: TaskDAGBuilder<E> = TaskDAGBuilder<E>()
    builder.block()
    return TaskDAG(builder.tasks, builder.trans)
}

