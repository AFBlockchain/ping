package hk.edu.polyu.af.bc.ping.flows

import co.paralleluniverse.fibers.Suspendable
import hk.edu.polyu.af.bc.ping.contracts.PingContract
import hk.edu.polyu.af.bc.ping.states.PingState
import net.corda.core.flows.FinalityFlow
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.FlowSession
import net.corda.core.flows.InitiatedBy
import net.corda.core.flows.InitiatingFlow
import net.corda.core.flows.ReceiveFinalityFlow
import net.corda.core.flows.StartableByRPC
import net.corda.core.node.services.Vault
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.transactions.TransactionBuilder
import java.lang.IllegalArgumentException
import java.util.UUID

@StartableByRPC
@InitiatingFlow
class Pong(
    private val pong: String,
    private val uuid: String
) : FlowLogic<String>() {
    @Suspendable
    override fun call(): String {
        val inputCriteria = QueryCriteria.LinearStateQueryCriteria(uuid = listOf(UUID.fromString(uuid)), status = Vault.StateStatus.UNCONSUMED)
        val pingStateRef = serviceHub.vaultService.queryBy(PingState::class.java, criteria = inputCriteria).states.singleOrNull()
            ?: throw IllegalArgumentException("PingState not found for uuid: $uuid")
        val pingState = pingStateRef.state.data

        logger.info("Ponging to ${pingState.pinger.name}, uuid = $uuid")

        val output = PingState(
            ping = pingState.ping,
            pong = pong,
            pinger = pingState.pinger,
            ponger = ourIdentity,
            linearId = pingState.linearId
        )

        val builder = TransactionBuilder(serviceHub.networkMapCache.notaryIdentities[0])
            .addCommand(PingContract.Commands.Pong(), ourIdentity.owningKey)
            .addInputState(pingStateRef)
            .addOutputState(output)

        builder.verify(serviceHub)
        val stx = serviceHub.signInitialTransaction(builder)
        subFlow(FinalityFlow(stx, initiateFlow(pingState.pinger)))

        logger.info("Ponged ${pingState.pinger.name}, linearId = ${output.linearId}")

        return output.linearId.toString()
    }
}

@InitiatedBy(Pong::class)
class PongResponder(
    private val session: FlowSession
) : FlowLogic<Unit>() {
    @Suspendable
    override fun call() {
        val stx = subFlow(ReceiveFinalityFlow(session))
        val ping = stx.coreTransaction.outputsOfType<PingState>().first()

        logger.info("Received ponging from ${session.counterparty.name}, pong = ${ping.pong}, linearId = ${ping.linearId}")
    }
}
