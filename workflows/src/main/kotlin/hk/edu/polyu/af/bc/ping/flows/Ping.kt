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
import net.corda.core.identity.Party
import net.corda.core.transactions.TransactionBuilder

@StartableByRPC
@InitiatingFlow
class Ping(
    private val ping: String,
    private val to: Party,
    private val notary: Party
) : FlowLogic<String>() {
    @Suspendable
    override fun call(): String {
        logger.info("Pinging ${to.name}")

        val output = PingState(
            ping = ping,
            pong = null,
            pinger = ourIdentity,
            ponger = to
        )

        val builder = TransactionBuilder(notary)
            .addCommand(PingContract.Commands.Ping(), ourIdentity.owningKey)
            .addOutputState(output)

        builder.verify(serviceHub)
        val stx = serviceHub.signInitialTransaction(builder)
        subFlow(FinalityFlow(stx, initiateFlow(to)))

        logger.info("Pinged ${to.name}, linearId = ${output.linearId}")

        return output.linearId.toString()
    }
}

@InitiatedBy(Ping::class)
class PingResponder(
    private val session: FlowSession
) : FlowLogic<Unit>() {
    @Suspendable
    override fun call() {
        val stx = subFlow(ReceiveFinalityFlow(session))
        val ping = stx.coreTransaction.outputsOfType<PingState>().first()

        logger.info("Received pinging from ${session.counterparty.name}, ping = ${ping.ping}, linearId = ${ping.linearId}")
    }
}
