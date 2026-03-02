package thedevibrahim.workflows.core.types

typealias WithWorkflowContextCallback<TWorkflowContext> =
    suspend (workflowContext: TWorkflowContext) -> Unit
