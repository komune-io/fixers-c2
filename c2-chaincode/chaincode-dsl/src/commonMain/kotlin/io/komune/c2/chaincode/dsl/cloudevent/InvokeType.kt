package io.komune.c2.chaincode.dsl.cloudevent

/**
 * CloudEvents `type` attribute constants for the `/invoke` API.
 *
 * Single source of truth shared between gateway emitter and SDK decoder
 * via the KMP common DSL. Drift between the two is a compile error.
 */
object InvokeType {
    object Request {
        const val GENERIC = "io.komune.c2.invoke"
        const val SSM_START = "io.komune.c2.ssm.session.start"
        const val SSM_PERFORM = "io.komune.c2.ssm.session.perform"
        const val SSM_CREATE = "io.komune.c2.ssm.create"
        const val USER_REGISTER = "io.komune.c2.user.register"
    }

    object Outcome {
        const val PREFIX = "io.komune.c2.invoke.outcome."
        const val COMMITTED = "${PREFIX}committed"
        const val REJECTED = "${PREFIX}rejected"
        const val TRANSIENT = "${PREFIX}transient"
        const val INDETERMINATE = "${PREFIX}indeterminate"
        const val CONFLICT = "${PREFIX}conflict"
    }
}
