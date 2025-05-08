package io.komune.c2.chaincode.dsl


interface EndorserDTO {
    /**
     * The peer name
     */
    val peer: String
    /**
     * The organisation name
     */
    val organisation: String
}

data class Endorser(
    override val peer: String,
    override val organisation: String
): EndorserDTO {

    companion object {
        /**
         * Create a list of Endorser from a string list of endorser
         * peer:organisation,peer:organisation
         */
        fun fromListPair(endorsers: String): List<Endorser> {
            return endorsers.split(",").map { endorserValue ->
                fromStringPair(endorserValue)
            }
        }

        /**
         * Create an Endorser from a string pair of endorser
         * peer:organisation
         */
        fun fromStringPair(pair: String): Endorser {
            val endorser = pair.split(":")
            require(endorser.size == 2) { "Bad endorser argument[${pair}]. Syntax must by peer:organisation" }
            return Endorser(endorser[0], endorser[1])
        }
    }
}
