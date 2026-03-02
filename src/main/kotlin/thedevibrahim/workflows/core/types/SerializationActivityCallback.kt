package thedevibrahim.workflows.core.types

typealias SerializationActivityCallback<TWorkflowContext, TActivityContext, TReturnedContext> =
    suspend (workflowContext: TWorkflowContext, activityContext: TActivityContext) -> TReturnedContext
