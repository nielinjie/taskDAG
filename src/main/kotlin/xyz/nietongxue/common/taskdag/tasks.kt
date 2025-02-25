package xyz.nietongxue.common.taskdag

import org.springframework.messaging.support.MessageBuilder
import org.springframework.statemachine.StateContext
import org.springframework.statemachine.StateMachine
import org.springframework.statemachine.action.Action
import org.springframework.statemachine.config.StateMachineBuilder
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer
import org.springframework.statemachine.listener.StateMachineListenerAdapter
import reactor.core.publisher.Mono
import java.util.concurrent.CountDownLatch

typealias Context = Map<String, Any>
typealias ActionResult<E> = Pair<E, Context>

interface Task<E : Any> {
    val name: String
    fun action(context: Context): ActionResult<E>
    fun exception(context: Context, e: Exception): E
}

abstract class RichTask<E : Any>(val meta: Map<String, Any>) : Task<E> {
}

abstract class InitTask<E : Any> : Task<E> {
    override fun action(context: Context): ActionResult<E> {
        error("init node, should not be called")
    }


    override fun exception(context: Context, e: Exception): E {
        error("init node, should not be called")
    }
}

abstract class EndTask<E : Any> : Task<E> {
    override fun action(context: Context): ActionResult<E> {
        error("init node, should not be called")
    }


    override fun exception(context: Context, e: Exception): E {
        error("init node, should not be called")
    }
}

data class Trans<E : Any>(val from: String, val to: String, val event: E)


class TaskWrap<E : Any>(val task: Task<E>) {
    val name = task.name
}

fun <E : Any> TaskDAG<E>.start(event: E, context: Context = emptyMap()): TasksRuntime<E> {
    return TasksRuntime(this).also {
        it.start(event, context)
    }
}

class TasksRuntime<E : Any>(
    val dag: TaskDAG<E>,
) {
    init {
        dag.validate().getOrThrow()
    }

    val logger = org.slf4j.LoggerFactory.getLogger(TasksRuntime::class.java)

    val builder = StateMachineBuilder.builder<String, E>()
    val tasksW = dag.tasks.map { TaskWrap(it) }
    val init = tasksW.first { it.task is InitTask<*> }
    val ends = tasksW.filter { it.task is EndTask<*> }
    val normalTasks = tasksW.filter { it.task !is InitTask<*> && it.task !is EndTask<*> }
    fun Trans<E>.build(builder: StateMachineTransitionConfigurer<String, E>) {
//        if (this.from == this.to) {
//            builder.withInternal().source(this.from).event(event)
//        } else {
        builder
            .withExternal()
            .source(this.from).target(to).event(event)
    }

    val countdown: CountDownLatch = CountDownLatch(1)

    val sm = builder.also {
        it.configureStates().withStates().also {
            val name = init.name
            it.initial(name)
                .state(name)
        }
            .also { stateBuilder ->
                normalTasks.forEach {
                    stateBuilder.state(it.name, action(it.task))
                }
            }.also { stateBuilder ->
                ends.forEach {
                    stateBuilder.end(it.name).state(it.name)
                }
            }
        it.configureTransitions()
            .also { transitionBuilder ->
                dag.trans.forEach {
                    it.build(transitionBuilder)
                }
            }
    }.build().also {
        it.addStateListener(object : StateMachineListenerAdapter<String, E>() {
            override fun stateMachineStopped(stateMachine: StateMachine<String, E>?) {
                this@TasksRuntime.countdown.countDown()
                super.stateMachineStopped(stateMachine)
            }
        })
    }

    fun start(event: E, context: Context = emptyMap()) {
        sm.extendedState.variables.putAll(context)
        sm.startReactively().subscribe()
        sm.sendEvent(Mono.just(MessageBuilder.withPayload(event).build())).subscribe()
    }

    fun waitForEnd(): Context {
        countdown.await()
        return this.sm.extendedState.variables as Context
    }

    fun action(task: Task<E>): Action<String, E> {
        return object : Action<String, E> {
            override fun execute(context: StateContext<String, E>?) {
                logger.debug("executing task ${task.name}")
                val c = context?.stateMachine?.extendedState?.variables as? Context
                try {
                    val (result, effect) = task.action(c ?: emptyMap())
                    context?.stateMachine?.extendedState?.variables?.putAll(effect)
                    logger.debug("task ${task.name} executed, result: $result")
                    context?.stateMachine?.sendEvent(Mono.just(MessageBuilder.withPayload(result).build()))
                        ?.subscribe()
                    logger.debug("task ${task.name} event sent")
                } catch (e: Exception) {
                    val result = task.exception(c ?: emptyMap(), e)
                    context?.stateMachine?.sendEvent(Mono.just(MessageBuilder.withPayload(result).build()))
                        ?.subscribe()
                }
            }
        }
    }
}