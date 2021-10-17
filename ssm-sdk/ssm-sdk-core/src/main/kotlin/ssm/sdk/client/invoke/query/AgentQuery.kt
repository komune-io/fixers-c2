package ssm.sdk.client.invoke.query

import ssm.sdk.client.invoke.builder.QueryBuilder
import ssm.sdk.client.invoke.builder.HasGet
import ssm.sdk.client.invoke.builder.HasList
import ssm.sdk.client.model.SsmQueryName

class AgentQuery : QueryBuilder(SsmQueryName.USER), HasGet, HasList
