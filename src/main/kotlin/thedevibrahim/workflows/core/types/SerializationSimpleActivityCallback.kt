package thedevibrahim.workflows.core.types

typealias SerializationSimpleActivityCallback<TWorkflowContext, TReturnedContext> =
    suspend (workflowContext: TWorkflowContext) -> TReturnedContext
