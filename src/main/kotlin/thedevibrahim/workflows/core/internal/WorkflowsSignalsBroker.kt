package thedevibrahim.workflows.core.internal

import kotlinx.serialization.json.Json
import thedevibrahim.workflows.core.annotations.WorkflowsPerformance
import thedevibrahim.workflows.core.interfaces.KeyValueClient
import thedevibrahim.workflows.core.interfaces.WorkflowsExceptionHandler
import thedevibrahim.workflows.core.interfaces.runSafe
import thedevibrahim.workflows.core.internal.dtos.WorkflowSignalMessageDto
import thedevibrahim.workflows.core.internal.enums.WorkflowSignal
import thedevibrahim.workflows.core.types.WorkflowId
import thedevibrahim.workflows.core.types.workflowKey

@OptIn(WorkflowsPerformance::class)
internal class WorkflowsSignalsBroker(
    private val keyValueClient: KeyValueClient,
    private val workflowsExceptionHandler: WorkflowsExceptionHandler,
    private val workflowsRunner: WorkflowsRunner,
) {
    private inline val json: Json
        get() = Json.Default

    suspend fun cancel(workflowId: WorkflowId) {
        set(workflowId, WorkflowSignal.CANCEL)

        if (workflowsRunner.contains(workflowId)) {
            workflowsRunner.cancel(workflowId)
        } else {
            publish(workflowId, WorkflowSignal.CANCEL)
        }
    }

    suspend fun init() {
        keyValueClient.subscribe(WorkflowSignal.CHANNEL) { messageString ->
            workflowsExceptionHandler.runSafe {
                val message = json.decodeFromString<WorkflowSignalMessageDto>(messageString)

                when (message.signal) {
                    WorkflowSignal.CANCEL -> {
                        workflowsRunner.cancel(message.workflowId)
                    }
                }
            }
        }
    }

    private suspend inline fun set(
        workflowId: WorkflowId,
        signal: WorkflowSignal,
    ) {
        keyValueClient.hSetIfKeyExistsScript(workflowId.workflowKey, WorkflowSignal.FIELD_KEY to signal.toString())
    }

    private suspend inline fun publish(
        workflowId: WorkflowId,
        signal: WorkflowSignal,
    ) {
        keyValueClient.publish(
            WorkflowSignal.CHANNEL,
            json.encodeToString(
                WorkflowSignalMessageDto(
                    workflowId = workflowId,
                    signal = signal,
                ),
            ),
        )
    }
}
