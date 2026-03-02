package thedevibrahim.workflows.core.internal.enums

import thedevibrahim.workflows.core.coroutines.getActivityId
import thedevibrahim.workflows.core.internal.consts.ACTIVITY_FIELD_KEY_PREFIX
import kotlin.coroutines.coroutineContext

internal enum class ActivityStatus {
    COMPLETED,
    ;

    companion object {
        suspend inline fun getFieldKey(): String = "${ACTIVITY_FIELD_KEY_PREFIX}:${coroutineContext.getActivityId()}:status"
    }
}
