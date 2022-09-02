package hk.edu.polyu.af.bc.ping.states

import hk.edu.polyu.af.bc.ping.contracts.PingContract
import net.corda.core.contracts.BelongsToContract
import net.corda.core.contracts.ContractState
import net.corda.core.contracts.LinearState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.AbstractParty
import net.corda.core.identity.Party

@BelongsToContract(PingContract::class)
data class PingState(
    val ping: String?,
    val pong: String?,
    val pinger: Party,
    val ponger: Party,
    override val linearId: UniqueIdentifier = UniqueIdentifier(),
    override val participants: List<AbstractParty> = listOf(pinger, ponger)
) : ContractState, LinearState
