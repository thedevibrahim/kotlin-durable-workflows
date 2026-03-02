package thedevibrahim.workflows.core.internal.dtos

import kotlinx.serialization.Serializable
import thedevibrahim.workflows.core.internal.enums.WorkflowSignal
import thedevibrahim.workflows.core.types.WorkflowId

@Serializable
internal data class WorkflowSignalMessageDto(
    val workflowId: WorkflowId,
    val signal: WorkflowSignal,
)
