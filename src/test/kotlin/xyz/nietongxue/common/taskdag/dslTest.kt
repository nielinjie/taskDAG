package xyz.nietongxue.common.taskdag

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import xyz.nietongxue.common.taskdag.stringEvent.EventDefaults.START
import xyz.nietongxue.common.taskdag.stringEvent.EventDefaults.SUCCESS
import xyz.nietongxue.common.taskdag.stringEvent.startFrom

class DslTest {
    @Test
    fun dsl() {
        val dag = dag<String> {
            init().to(
                action("task1") {
                    "task12end" to it
                }).on("init2task1")
            "task1".to(end()).on("task12end")
        }
        assertThat(dag).isNotNull
        assertThat(dag.tasks).hasSize(3)
        assertThat(dag.trans).hasSize(2)
    }


    @Test
    fun dsl2() {
        val dag = dag {
            init().to(
                action("task1") {
                    "1_2" to it
                }).on("init_1")
            "task1".to(action("task2") {
                "2_end" to it
            }).on("1_2")
            "task2".to(end()).on("2_end")
        }
        assertThat(dag).isNotNull
        assertThat(dag.tasks).hasSize(4)
        assertThat(dag.trans).hasSize(3)
    }

    @Test
    fun shortcuts() {
        val dag = dag {
            init().to(
                action("task1") {
                    "1_2" to it
                }).on("init_1")
            "task1".to(fire("task2", "2_end")).on("1_2")
            "task2".to(end()).on("2_end")
        }
        assertThat(dag).isNotNull
        assertThat(dag.tasks).hasSize(4)
    }

    @Test
    fun dslValidate() {
        assertThrows<Exception> {
            dag {
                action("task1") {
                    "1_2" to it
                }
                action("task2") {
                    "2_end" to it
                }
//                init()
                end()
                "INIT".to("task1").on("init_1")
                "task1".to("task2").on("1_2")
                "task2".to("END").on("2_end")
            }
        }
    }

    @Test
    fun dslValidate2() {
        assertThrows<Exception> {
            dag {
                action("task1") {
                    "1_2" to it
                }
                action("task2") {
                    "2_end" to it
                }
                init()
                end()
                "init".to("task1").on("init_1") //should be "INIT"
                "task1".to("task2").on("1_2")
                "task2".to("END").on("2_end")
            }
        }
    }

    @Test
    fun dslNew() {
        dag {
            mock("t1", SUCCESS)
            startFrom("t1")
            "t1".to(end()).on(SUCCESS)
        }.start(START).also {
            it.waitForEnd()
        }

    }
}