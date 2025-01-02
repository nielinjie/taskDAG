package xyz.nietongxue.common.taskdag

import org.slf4j.LoggerFactory
import xyz.nietongxue.common.base.md5

class Diagram<E : Any>(val taskDAG: TaskDAG<E>) {
    val normalTasks = taskDAG.tasks.filter { it !is InitTask<*> && it !is EndTask<*> }
    val ends = taskDAG.tasks.filter { it is EndTask<*> }
    val start = taskDAG.tasks.first() { it is InitTask<*> }

    private val logger = LoggerFactory.getLogger(Diagram::class.java)
    fun toPlantUML(extendNested: Boolean = true): String {
        val sb = StringBuilder()
        sb.appendLine("@startuml")
        sb.appendLine(this.toPlantUMLInner(extendNested = extendNested))
        sb.appendLine("@enduml")
        return sb.toString()
    }


    private fun toPlantUMLInner(prefix: String = "", extendNested: Boolean = true): String {
        fun nameToId(name: String): String {
            return (prefix + "_" + name).also {
                logger.debug("nameToId: $it")
            }.md5().substring(0, 8)
        }

        val sb = StringBuilder()
        normalTasks.forEach {
            if (extendNested && it is RichTask<*> && it.meta["dag"] as? NestedDAG<*> != null) {
                sb.appendLine("rectangle \"${it.name}\" as ${nameToId(it.name)} {")
                val subDag = Diagram((it.meta["dag"] as NestedDAG<*>).first)
                sb.appendLine(subDag.toPlantUMLInner(prefix + "_" + it.name, extendNested))
                sb.appendLine("}")
            } else {
                sb.appendLine("rectangle \"${it.name}\" as ${nameToId(it.name)} ")
            }
        }
        start.also {
            sb.appendLine("circle \"${it.name}\" as ${nameToId(it.name)} ")
        }
        ends.forEach {
            sb.appendLine("circle \"${it.name}\" as ${nameToId(it.name)} ")
        }
        taskDAG.trans.forEach {
            sb.appendLine("${nameToId(it.from)} --> ${nameToId(it.to)} : ${it.event}")
        }
        return sb.toString()

    }

}