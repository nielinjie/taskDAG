package xyz.nietongxue.common.taskdag

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class TaskDagApplication

fun main(args: Array<String>) {
    runApplication<TaskDagApplication>(*args)
}
