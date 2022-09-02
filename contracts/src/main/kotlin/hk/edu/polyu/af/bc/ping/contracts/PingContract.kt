package hk.edu.polyu.af.bc.ping.contracts

import hk.edu.polyu.af.bc.ping.states.PingState
import net.corda.core.contracts.CommandData
import net.corda.core.contracts.Contract
import net.corda.core.contracts.requireSingleCommand
import net.corda.core.contracts.requireThat
import net.corda.core.transactions.LedgerTransaction

class PingContract : Contract {
    interface Commands : CommandData {
        class Ping : Commands
        class Pong : Commands
    }

    override fun verify(tx: LedgerTransaction) {
        val command = tx.commands.requireSingleCommand<Commands>()
        val output = tx.outputsOfType<PingState>().first()

        when (command.value) {
            is Commands.Ping -> requireThat {
                "There must be no input states when pinging" using tx.inputStates.isEmpty()

                "Must have ping" using (output.ping != null)
                "Must not have pong" using (output.pong == null)

                "Pinger must sign" using (command.signers.contains(output.pinger.owningKey))
            }

            is Commands.Pong -> requireThat {
                "There must be single input state when ponging" using (tx.inputStates.size == 1)
                val input = tx.inputsOfType<PingState>().first()

                "Input and output must have the same linearId" using (input.linearId == output.linearId)
                "Ping must not change" using (input.ping == output.ping)
                "Original pong must be null" using (input.pong == null)
                "Must have pong" using (output.pong != null)

                "Ponger must sign" using (command.signers.contains(output.ponger.owningKey))
            }
        }
    }
}
