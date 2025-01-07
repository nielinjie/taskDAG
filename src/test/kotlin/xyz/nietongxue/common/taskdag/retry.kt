package xyz.nietongxue.common.taskdag

import org.junit.jupiter.api.Test
import xyz.nietongxue.common.taskdag.EventDefaults.EXCEPTION
import xyz.nietongxue.common.taskdag.EventDefaults.START
import xyz.nietongxue.common.taskdag.EventDefaults.SUCCESS
import xyz.nietongxue.common.taskdag.TaskDefaults.END
import xyz.nietongxue.common.taskdag.TaskDefaults.INIT

class RetryTest() {
    @Test
    fun retryTest() {
        val dag = dag {
            init()
            end()
            retry(mock("t1", EXCEPTION, SUCCESS), 3)
            mock("t2", SUCCESS)
            INIT.to("t1").on(START)
            "t1".on(SUCCESS).to("t2")
            "t2".on(SUCCESS).to(END)
        }
        dag.start(START).also {
            it.waitForEnd()
        }
    }

    @Test
    fun retryTest2() {
        val dag = dag {
            init()
            end()
            retry(mock("t1", EXCEPTION, EXCEPTION, EXCEPTION, EXCEPTION), 3)
            mock("t2", SUCCESS)
            INIT.to("t1").on(START)
            "t1".on(SUCCESS).to("t2")
            "t2".on(SUCCESS).to(END)
            defaultCatching()
        }
        dag.start(START).also {
            it.waitForEnd()
        }
    }

    @Test
    fun retryTest3() {
        val dag = dag {
            init()
            end()
            retry(mock("t1", EXCEPTION, EXCEPTION, SUCCESS), 3)
            mock("t2", EXCEPTION)
            INIT.to("t1").on(START)
            "t1".on(SUCCESS).to("t2")
            "t2".on(SUCCESS).to(END)
            defaultCatching()
        }
        dag.start(START).also {
            it.waitForEnd()
        }
    }

    @Test
    fun retryTest4() {
        val dag = dag {
            init()
            end()
            retry(mock("t1", EXCEPTION, EXCEPTION, SUCCESS + "2"), 3)
            mock("t2", EXCEPTION)
            mock("t3", SUCCESS)
            INIT.to("t1").on(START)
            "t1".on(SUCCESS).to("t2")
            "t1".on(SUCCESS + "2").to("t3")
            "t2".on(SUCCESS).to(END)
            "t3".on(SUCCESS).to(END)
            defaultCatching()
        }
        dag.start(START).also {
            it.waitForEnd()
        }
    }
}