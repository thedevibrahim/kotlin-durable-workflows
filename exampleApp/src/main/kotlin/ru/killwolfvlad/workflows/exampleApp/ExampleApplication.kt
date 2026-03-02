package thedevibrahim.workflows.exampleApp

import eu.vendeli.rethis.ReThis
import io.ktor.http.Url
import kotlinx.coroutines.SupervisorJob
import thedevibrahim.workflows.clients.ReThisRedisClient
import thedevibrahim.workflows.core.WorkflowsConfig
import thedevibrahim.workflows.core.WorkflowsWorker
import thedevibrahim.workflows.core.annotations.WorkflowsPerformance
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

@OptIn(WorkflowsPerformance::class)
suspend fun main() {
    val redisConnectionString = "redis://localhost:6379"

    val rootJob = SupervisorJob()

    val workflowsWorker =
        WorkflowsWorker(
            WorkflowsConfig(
                workerId = "example-worker-1",
                heartbeatInterval = 15.seconds,
                lockTimeout = 1.minutes,
                fetchInterval = 2.minutes,
                rootJob = rootJob,
            ),
            ReThisRedisClient(
                Url(redisConnectionString).let {
                    ReThis(it.host, it.port)
                },
            ),
            WorkflowsClassManagerImpl(),
            WorkflowsExceptionHandlerImpl(),
        )

    workflowsWorker.init()

    workflowsWorker.executeExampleWorkflow(
        ExampleWorkflow.DelayContext(duration = 1.minutes),
        ExampleWorkflow.DeleteMessageContext(chatId = -10L, messageId = 10),
    )

    rootJob.join() // to prevent exit from application, no need e.g. in ktor applications
}
