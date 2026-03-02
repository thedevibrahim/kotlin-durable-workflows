package thedevibrahim.workflows.core.coroutines

import thedevibrahim.workflows.core.ActivityContext
import thedevibrahim.workflows.core.WorkflowContext
import thedevibrahim.workflows.core.annotations.WorkflowsPerformance
import thedevibrahim.workflows.core.interfaces.KeyValueClient
import thedevibrahim.workflows.core.interfaces.Workflow
import thedevibrahim.workflows.core.types.WorkflowId
import thedevibrahim.workflows.core.types.workflowKey
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext

@OptIn(WorkflowsPerformance::class)
data class WorkflowCoroutineContext(
    val workflowId: WorkflowId,
    val workflowContext: WorkflowContext,
    val activityContext: ActivityContext,
    val keyValueClient: KeyValueClient,
) : AbstractCoroutineContextElement(WorkflowCoroutineContext) {
    companion object Key : CoroutineContext.Key<WorkflowCoroutineContext>
}

suspend inline fun CoroutineContext.getWorkflowId(): WorkflowId =
    coroutineContext[WorkflowCoroutineContext]?.workflowId
        ?: throw NullPointerException("${WorkflowCoroutineContext::class.simpleName} must be in coroutineContext!")

@OptIn(WorkflowsPerformance::class)
suspend inline fun CoroutineContext.getWorkflowContext(): WorkflowContext =
    coroutineContext[WorkflowCoroutineContext]?.workflowContext
        ?: throw NullPointerException("${WorkflowCoroutineContext::class.simpleName} must be in coroutineContext!")

@OptIn(WorkflowsPerformance::class)
suspend inline fun CoroutineContext.getActivityContext(): ActivityContext =
    coroutineContext[WorkflowCoroutineContext]?.activityContext
        ?: throw NullPointerException("${WorkflowCoroutineContext::class.simpleName} must be in coroutineContext!")

@OptIn(WorkflowsPerformance::class)
suspend inline fun CoroutineContext.getKeyValueClient(): KeyValueClient =
    coroutineContext[WorkflowCoroutineContext]?.keyValueClient
        ?: throw NullPointerException("${WorkflowCoroutineContext::class.simpleName} must be in coroutineContext!")

internal suspend inline fun CoroutineContext.getWorkflowKey(): String =
    coroutineContext[WorkflowCoroutineContext]?.workflowId?.workflowKey
        ?: throw NullPointerException("${WorkflowCoroutineContext::class.simpleName} must be in coroutineContext!")

suspend inline fun Workflow.getWorkflowId(): WorkflowId = coroutineContext.getWorkflowId()

@OptIn(WorkflowsPerformance::class)
suspend inline fun Workflow.getWorkflowContext(): WorkflowContext = coroutineContext.getWorkflowContext()

@OptIn(WorkflowsPerformance::class)
suspend inline fun Workflow.getActivityContext(): ActivityContext = coroutineContext.getActivityContext()
