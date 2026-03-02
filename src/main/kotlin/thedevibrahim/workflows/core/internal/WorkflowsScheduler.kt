package thedevibrahim.workflows.core.internal

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import thedevibrahim.workflows.core.WorkflowsConfig
import thedevibrahim.workflows.core.annotations.WorkflowsPerformance
import thedevibrahim.workflows.core.interfaces.KeyValueClient
import thedevibrahim.workflows.core.interfaces.WorkflowsExceptionHandler
import thedevibrahim.workflows.core.interfaces.runSafe
import thedevibrahim.workflows.core.internal.consts.WORKFLOW_CLASS_NAME_FIELD_KEY
import thedevibrahim.workflows.core.internal.consts.WORKFLOW_LOCKS_KEY
import thedevibrahim.workflows.core.internal.consts.WORKFLOW_WORKERS_KEY
import thedevibrahim.workflows.core.internal.extensions.workflowClass
import thedevibrahim.workflows.core.types.WorkflowId
import thedevibrahim.workflows.core.types.workflowKey

@OptIn(WorkflowsPerformance::class)
internal class WorkflowsScheduler(
    rootJob: Job,
    private val config: WorkflowsConfig,
    private val keyValueClient: KeyValueClient,
    private val workflowsExceptionHandler: WorkflowsExceptionHandler,
    private val workflowsRunner: WorkflowsRunner,
) {
    private val coroutineScope =
        CoroutineScope(
            rootJob + Dispatchers.IO + CoroutineName(WorkflowsScheduler::class.simpleName + "Coroutine"),
        )

    private lateinit var fetchJob: Job

    suspend fun init() {
        fetchWorkflows()

        fetchJob =
            coroutineScope.launch {
                while (true) {
                    delay(config.fetchInterval)

                    workflowsExceptionHandler.runSafe {
                        fetchWorkflows()
                    }
                }
            }
    }

    private suspend inline fun fetchWorkflows() {
        val (locks, workers) = keyValueClient.pipelineHGetAll(WORKFLOW_LOCKS_KEY, WORKFLOW_WORKERS_KEY)

        val workflowIdsToRun =
            locks
                .filter filterWorkflows@{
                    if (workflowsRunner.contains(WorkflowId(it.key))) {
                        return@filterWorkflows false
                    }

                    if (it.value == config.workerId) {
                        return@filterWorkflows true
                    }

                    return@filterWorkflows !workers.contains(it.value)
                }.map {
                    WorkflowId(it.key)
                }

        if (workflowIdsToRun.isEmpty()) {
            return
        }

        val runPayloads =
            keyValueClient
                .pipelineHGet(
                    *workflowIdsToRun.map { it.workflowKey to WORKFLOW_CLASS_NAME_FIELD_KEY }.toTypedArray(),
                ).zip(workflowIdsToRun) { a, b ->
                    object {
                        val workflowId = b
                        val workflowClass = a?.workflowClass
                    }
                }

        runPayloads
            .map { runPayload ->
                coroutineScope.async run@{
                    if (runPayload.workflowClass == null) {
                        return@run
                    }

                    workflowsRunner.run(runPayload.workflowId, emptyMap(), runPayload.workflowClass)
                }
            }.awaitAll()
    }
}
