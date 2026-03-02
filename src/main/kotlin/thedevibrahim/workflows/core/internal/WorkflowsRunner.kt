package thedevibrahim.workflows.core.internal

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import thedevibrahim.workflows.core.ActivityContext
import thedevibrahim.workflows.core.WorkflowContext
import thedevibrahim.workflows.core.WorkflowsConfig
import thedevibrahim.workflows.core.annotations.WorkflowsPerformance
import thedevibrahim.workflows.core.coroutines.WorkflowCoroutineContext
import thedevibrahim.workflows.core.interfaces.KeyValueClient
import thedevibrahim.workflows.core.interfaces.Workflow
import thedevibrahim.workflows.core.interfaces.WorkflowsClassManager
import thedevibrahim.workflows.core.interfaces.WorkflowsExceptionHandler
import thedevibrahim.workflows.core.interfaces.runSafe
import thedevibrahim.workflows.core.internal.consts.WORKFLOW_CLASS_NAME_FIELD_KEY
import thedevibrahim.workflows.core.internal.consts.WORKFLOW_LOCKS_KEY
import thedevibrahim.workflows.core.internal.consts.WORKFLOW_WORKERS_KEY
import thedevibrahim.workflows.core.internal.enums.WorkflowSignal
import thedevibrahim.workflows.core.internal.extensions.workflowClassName
import thedevibrahim.workflows.core.types.WorkflowId
import thedevibrahim.workflows.core.types.workflowKey
import thedevibrahim.workflows.core.workflowContextFieldKey
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass

@OptIn(WorkflowsPerformance::class)
internal class WorkflowsRunner(
    rootJob: Job,
    private val activityContext: ActivityContext,
    private val config: WorkflowsConfig,
    private val keyValueClient: KeyValueClient,
    private val workflowContext: WorkflowContext,
    private val workflowsClassManager: WorkflowsClassManager,
    private val workflowsExceptionHandler: WorkflowsExceptionHandler,
) {
    private val coroutineScope =
        CoroutineScope(
            rootJob + Dispatchers.IO + CoroutineName(WorkflowsRunner::class.simpleName + "Coroutine"),
        )

    private val workflowJobs = ConcurrentHashMap<WorkflowId, Job>()

    val activeWorkflows: Int
        get() = workflowJobs.size

    suspend fun run(
        workflowId: WorkflowId,
        initialContext: Map<String, String>,
        workflowClass: KClass<out Workflow>,
    ) {
        if (contains(workflowId)) {
            return
        }

        if (!acquireWorkflowLock(workflowId, initialContext, workflowClass)) {
            return
        }

        launchWorkflow(workflowId, workflowClass)
    }

    fun cancel(workflowId: WorkflowId) {
        workflowJobs[workflowId]?.cancel()
    }

    fun contains(workflowId: WorkflowId): Boolean = workflowJobs.containsKey(workflowId)

    private suspend inline fun acquireWorkflowLock(
        workflowId: WorkflowId,
        initialContext: Map<String, String>,
        workflowClass: KClass<out Workflow>,
    ): Boolean =
        keyValueClient.acquireWorkflowLock(
            // keys
            workflowKey = workflowId.workflowKey,
            workflowLocksKey = WORKFLOW_LOCKS_KEY,
            workflowWorkersKey = WORKFLOW_WORKERS_KEY,
            // arguments
            workflowId = workflowId,
            workerId = config.workerId,
            // workflow context
            workflowClassNameFieldKey = WORKFLOW_CLASS_NAME_FIELD_KEY,
            workflowClassName = workflowClass.workflowClassName,
            initialContext = initialContext.mapKeys { it.key.workflowContextFieldKey },
        ) >= 1L

    private fun launchWorkflow(
        workflowId: WorkflowId,
        workflowClass: KClass<out Workflow>,
    ) {
        val job =
            coroutineScope
                .launch(
                    WorkflowCoroutineContext(workflowId, workflowContext, activityContext, keyValueClient),
                    CoroutineStart.LAZY,
                ) workflow@{
                    workflowsExceptionHandler
                        .runSafe {
                            val signal =
                                keyValueClient.hGet(workflowId.workflowKey, WorkflowSignal.FIELD_KEY)?.let {
                                    WorkflowSignal.valueOf(it)
                                }

                            if (signal != WorkflowSignal.CANCEL) {
                                val workflow = workflowsClassManager.getInstance(workflowClass)

                                workflow.execute()
                            }
                        }.onFailure {
                            return@workflow
                        }

                    withContext(NonCancellable) {
                        workflowsExceptionHandler.runSafe {
                            keyValueClient.deleteWorkflow(
                                // keys
                                workflowKey = workflowId.workflowKey,
                                workflowLocksKey = WORKFLOW_LOCKS_KEY,
                                // arguments
                                workflowId = workflowId,
                            )
                        }
                    }
                }.also {
                    it.invokeOnCompletion {
                        workflowJobs.remove(workflowId)
                    }
                }

        val actualJob = workflowJobs.putIfAbsent(workflowId, job)

        if (actualJob == null) {
            job.start()
        }
    }
}
