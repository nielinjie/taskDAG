# TaskDAG

一个任务图执行器。

以有向图描述任务和之间的执行顺序关系。使用 Spring Statemachine 实现。

一个应用案例是用来实现一个类似于 langgraph / langgraph4j 的 AI 任务编排。

## 示例

参阅 test 代码 - 
```
src/test/kotlin/xyz/nietongxue/common/taskdag/langgraph4jSample.kt
```