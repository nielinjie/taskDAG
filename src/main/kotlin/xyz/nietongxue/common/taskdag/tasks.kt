package xyz.nietongxue.common.taskdag

import org.springframework.messaging.support.MessageBuilder
import org.springframework.statemachine.action.Action
import org.springframework.statemachine.config.StateMachineBuilder
import reactor.core.publisher.Mono


interface Task {
    val name: String
    val action: () -> Unit
    //success value
    //failure value
    //effects
}

class InitTask : Task {
    override val name: String = "init"
    override val action: () -> Unit = { }
}

class EndTask : Task {
    override val name: String = "end"
    override val action: () -> Unit = { }
}

class Trans(from: String, to: String, event: String)

class TaskDag(
    val tasks: List<Task> = emptyList(),
    val trans: List<Trans> = emptyList()
)

class TaskWrap(task: Task) {
    val name = task.name
    val action = task.action
}

class TasksRuntime(
    val tasks: List<Task> = emptyList()
) {
    val builder = StateMachineBuilder.builder<String, String>()
    val tasksW = tasks.map { TaskWrap(it) }

    val sm = builder.also {
        it.configureStates().withStates()
            .initial("INIT")
            .state("INIT")
            .also { stateBuilder ->
                tasksW.forEach {
                    stateBuilder.state("TASK_${it.name}")
                }
            }
            .end("END")
        it.configureTransitions()
            .withExternal()
            .source("INIT").target("TASK").event("start")
            .and()
            .withExternal()
            .source("TASK").target("END").event("end")
    }.build()

    fun start() {
        sm.startReactively().subscribe()
        sm.sendEvent(Mono.just(MessageBuilder.withPayload("start").build())).subscribe()
    }

    fun action(string: String): Action<String, String> {
        return Action<String, String> { println(string) }
    }
}