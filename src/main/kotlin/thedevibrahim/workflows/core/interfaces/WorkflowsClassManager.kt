package thedevibrahim.workflows.core.interfaces

import kotlin.reflect.KClass

interface WorkflowsClassManager {
    fun getInstance(workflowClass: KClass<out Workflow>): Workflow
}
