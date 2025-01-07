package xyz.nietongxue.common.taskdag

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import xyz.nietongxue.common.taskdag.diagram.Diagram
import kotlin.io.path.Path
import kotlin.io.path.writeText

class SampleTest {
    val path = Path("./diagram.puml")

    @Test
    fun sample() {
        val dagI = dag {
            init()
            end()
            action("evaluate") {
                TODO()
            }
            action("review") {
                TODO()
            }
            "INIT".to("evaluate").on("init")
            "evaluate".to("review").on("error")
            "review".to("evaluate").on("retry")
            "evaluate".to("END").on("unknown")
            "evaluate".to("END").on("ok")
        }
        val dag = dag {
            init()
            end()
            action("describer") {
                TODO()
            }
            action("sequence") {
                TODO()
            }
            action("generic") {
                TODO()
            }
            dag(name = "evaluator", dag = dagI to "ok")

            "INIT".to("describer").on("init")
            "describer".to("sequence").on("sequence")
            "describer".to("generic").on("generic")
            "generic".to("evaluator").on("done")
            "sequence".to("evaluator").on("done")
            "evaluator".to("END").on("ok")
        }
        assertThat(dag).isNotNull
        val diagram = Diagram(dag)
        path.writeText(diagram.toPlantUML())
    }
}