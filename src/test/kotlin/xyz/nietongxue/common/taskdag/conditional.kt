package xyz.nietongxue.common.taskdag

import org.junit.jupiter.api.Test

class ConditionalTest() {
    private val adder = { it: Context ->
        longTimeFunc(5, "adder")
        println("add 1")
        "added" to it.toMutableMap().also {
            it.put("sum", 1 + (it.get("sum") ?: 0) as Int)
        }
    }
    private val lessThan3 = { it: Context ->
        val count = ((it.get("sum") ?: 0) as Int)
        println("count = $count")
        if (count < 3) {
            println("count < 3, to adder")
            "c_a" to it
        } else {
            println("count >= 3, to end")
            "c_e" to it
        }
    }

    val dag = dag {
        task("adder") {
            action = adder
        }
        task("condition") {
            action = lessThan3
        }
        init().to("adder").on("i_adder")
        "adder".to("condition").on("added")
        "condition".to("adder").on("c_a")
        "condition".to(end()).on("c_e")
    }
    val dag2 = dag {
        action("adder", adder)
        action("condition", lessThan3)
        init().to("adder").on("i_adder")
        "adder".to("condition").on("added")
        "condition".to("adder").on("c_a")
        "condition".to(end()).on("c_e")
    }

    @Test
    fun test() {
        val runtime = TasksRuntime(dag)
        runtime.start("i_adder")
        Thread.sleep(3000)
    }

    @Test
    fun test2() {
        val runtime = TasksRuntime(dag2)
        runtime.start("i_adder")
        Thread.sleep(3000)
    }

    @Test
    fun testStop() {
        val runtime = TasksRuntime(dag2)
        runtime.start("i_adder")
        runtime.waitForEnd()
    }
}