package xyz.nietongxue.common.taskdag

import org.junit.jupiter.api.Test

class SMTest {
    val dag = dag {
        action("task1") {
            longTimeFunc(5, "task1")
            "1_2" to it
        }
        action("task2") {
            longTimeFunc(3, "task2")
            "2_e" to it
        }
        init().to(
            "task1"
        ).on("i_1")
        "task1".to("task2").on("1_2")
        "task2".to(end()).on("2_e")
    }

    @Test
    fun test() {
        val runtime = TasksRuntime(dag)
        runtime.start("i_1")
        Thread.sleep(5000)
    }
}


fun longTimeFunc(count: Int, name: String) {
    var c = 0
    while (c < count) {
        println("$name - $c")
        c++
        Thread.sleep(100)
    }
}