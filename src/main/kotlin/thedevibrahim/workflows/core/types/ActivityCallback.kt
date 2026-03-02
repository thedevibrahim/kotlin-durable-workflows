package thedevibrahim.workflows.core.types

typealias ActivityCallback =
    suspend (workflowContextMap: Map<String, String?>, activityContextMap: Map<String, String?>) -> Map<String, String>?
