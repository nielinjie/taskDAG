package xyz.nietongxue.common.taskdag

import org.junit.jupiter.api.Test

class SMTest {

    @Test
    fun test() {
        val runtime = TasksRuntime()
//        runtime.addTask(object : Task {
//            override val name: String
//                get() = "task1"
//            override val action: () -> Unit
//                get() = { println("task1") }
//        })
//        runtime.addTask(object : Task {
//            override val name: String
//                get() = "task2"
//            override val action: () -> Unit
//                get() = { println("task2") }
//        })
        runtime.start()
    }
}
