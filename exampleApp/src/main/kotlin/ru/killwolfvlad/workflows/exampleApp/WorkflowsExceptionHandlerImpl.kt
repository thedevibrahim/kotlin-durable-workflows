package thedevibrahim.workflows.exampleApp

import io.ktor.util.logging.KtorSimpleLogger
import io.ktor.util.logging.error
import thedevibrahim.workflows.core.interfaces.WorkflowsExceptionHandler

class WorkflowsExceptionHandlerImpl : WorkflowsExceptionHandler {
    private val logger = KtorSimpleLogger("WorkflowsExceptionHandler")

    override suspend fun handle(exception: Exception) {
        logger.error(exception)
    }
}
