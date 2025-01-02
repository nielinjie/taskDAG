package xyz.nietongxue.common.taskdag

import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory

class NestedTest() {
    val logger = LoggerFactory.getLogger(NestedTest::class.java)
    private val adder = { it: Context ->
        longTimeFunc(5, "adder")
        logger.debug("add 1")
        "added" to it.toMutableMap().also {
            it.put("sum", 1 + (it.get("sum") ?: 0) as Int)
        }
    }
    private val lessThan3 = { it: Context ->
        val count: Int = (it.get("sum") ?: 0) as Int
        logger.debug("count = $count")
        if (count < 3) {
            logger.debug("count < 3, to adder")
            "c_a" to it
        } else {
            logger.debug("count >= 3, to end")
            "c_e" to it
        }
    }
    val dag1 =
        dag {
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

    @Test
    fun nestImpl() {

        val dag2 = dag {
            task("addTo3") {
                action = {
                    val runtime = TasksRuntime(dag1)
                    runtime.start("i_adder")
                    runtime.waitForEnd()
                    "added" to it
                }
            }
            init().to("addTo3").on("i_adder")
            "addTo3".to(end()).on("added")
        }
        val t = TasksRuntime(dag2)
        t.start("i_adder")
        t.waitForEnd()
    }

    @Test
    fun nestDsl() {
        val dag2 = dag {
            task("addTo3") {
                dag = dag1 to "added"
            }
            init().to("addTo3").on("i_adder")
            "addTo3".to(end()).on("added")
        }
        val t = TasksRuntime(dag2)
        t.start("i_adder")
        t.waitForEnd()
    }
}