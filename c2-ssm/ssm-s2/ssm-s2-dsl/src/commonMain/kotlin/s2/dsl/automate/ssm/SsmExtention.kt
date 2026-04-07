package s2.dsl.automate.ssm

import s2.dsl.automate.S2Automate
import s2.dsl.automate.S2Transition
import ssm.chaincode.dsl.model.Ssm
import ssm.chaincode.dsl.model.SsmTransition

fun S2Automate.toSsm(permissive: Boolean = false): Ssm {
	return Ssm(
		name = this.name,
		transitions = if (permissive) {
			this.transitions.toSsmTransitions(0, 0, withResultAsAction)
		} else {
			this.transitions.toSsmTransitions(withResultAsAction = withResultAsAction)
		}
	)
}

fun Array<out S2Transition>.toSsmTransitions(
	from: Int? = null, to: Int? = null, withResultAsAction: Boolean
) = filter { it.from != null }. map {
	it.toSsmTransition(from, to, withResultAsAction)
}

fun S2Transition.toSsmTransition(from: Int? = null, to: Int? = null, withResultAsAction: Boolean) = SsmTransition(
	from = from ?: this.from!!.position,
	to = to ?: this.to.position,
	role = this.role.name,
	action = this.result?.name?.takeIf { withResultAsAction } ?: this.action.name
)
