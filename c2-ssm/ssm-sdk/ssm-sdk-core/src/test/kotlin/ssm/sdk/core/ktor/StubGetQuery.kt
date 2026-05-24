package ssm.sdk.core.ktor

import ssm.sdk.core.invoke.builder.HasGet
import ssm.sdk.core.invoke.query.SsmQueryName

internal class StubGetQuery(override val queryName: SsmQueryName) : HasGet
