package xyz.nietongxue.common.taskdag

import org.junit.jupiter.api.Test
import kotlin.io.path.Path
import kotlin.io.path.writeText

class DiagramTest() {
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
    val path = Path("./diagram.puml")

    @Test
    fun diagram() {
        val diagram = Diagram(dag1)
        path.writeText(diagram.toPlantUML())
    }

    @Test
    fun diagramNest() {
        val dag2 = dag {
            task("addTo3") {
                dag = dag1 to "added"
            }
            init().to("addTo3").on("i_adder")
            "addTo3".to(end()).on("added")
        }
        val diagram = Diagram(dag2)
        path.writeText(diagram.toPlantUML())
    }

    @Test
    fun diagramNestNotExtend() {
        val dag2 = dag {
            task("addTo3") {
                dag = dag1 to "added"
            }
            init().to("addTo3").on("i_adder")
            "addTo3".to(end()).on("added")
        }
        val diagram = Diagram(dag2)
        path.writeText(diagram.toPlantUML(false))
    }

    @Test
    fun diagramNestNotExtend2() {
        val dag2 = dag {
            task("addTo3") {
                dag = dag1 to "added"
            }
            init().to("addTo3").on("i_adder")
            "addTo3".on("added").to(end())
        }
        val diagram = Diagram(dag2)
        path.writeText(diagram.toPlantUML(false))
    }

}