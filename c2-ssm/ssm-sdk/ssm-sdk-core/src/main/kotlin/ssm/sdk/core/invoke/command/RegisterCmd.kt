@file:Suppress("MaxLineLength")

package ssm.sdk.core.invoke.command

import ssm.chaincode.dsl.model.Agent
import ssm.sdk.core.invoke.builder.CmdBuilder
import ssm.sdk.dsl.SsmCmdName

// {
//    "name": "bob3",
//    "pub": "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAq5UG+Oy0R9nob5EtCxdL9T2GIGsHMCnEffULsC/54wpVydW7Dj6AHIA7IsfidXGjqdd8gqRTcfCV3ctfvAjDavwv/6pvADlXW8qsK3OSK6kx5qyIXm6AW93RIG6ZYPuIKQn0n2AjWWV630RqYdDk1mS3W43L1aKWlL3X2UI3ARwc3+jrWsailci4ijXlNSptYx+To2bUPSMP179TDM3icb1M7QQhNlEn2hnceVuNNntSAA5QhpYyeHdpu4EV1vXwuZK93VW5sJ8zG2pW1afTJ9x58bFbLYmyUFRQAcMinl50ilHIrVjJsUCGy2xRRYKCdqMOebMrGK6QSfUXLnd17wIDAQAB"
// }
//
// {
//  "InvokeArgs": [
//    "register",
//    "{\"name\":\"bob\",\"pub\":\"MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAq5UG+Oy0R9nob5EtCxdL9T2GIGsHMCnEffULsC/54wpVydW7Dj6AHIA7IsfidXGjqdd8gqRTcfCV3ctfvAjDavwv/6pvADlXW8qsK3OSK6kx5qyIXm6AW93RIG6ZYPuIKQn0n2AjWWV630RqYdDk1mS3W43L1aKWlL3X2UI3ARwc3+jrWsailci4ijXlNSptYx+To2bUPSMP179TDM3icb1M7QQhNlEn2hnceVuNNntSAA5QhpYyeHdpu4EV1vXwuZK93VW5sJ8zG2pW1afTJ9x58bFbLYmyUFRQAcMinl50ilHIrVjJsUCGy2xRRYKCdqMOebMrGK6QSfUXLnd17wIDAQAB\"}",
//    "adam",
//    "YptlDaWyOFDjJRTPB84484vsumu1pl/IPkVNqGyMMEdVARVr/MFXdRz4dQYUM5n4RXvReTT0kR/Tyvljs06e2w5HJpQIwCz4eeKcuXHgxvPrsrLxUF6QHn+SOTV0gRWqOdB5RI+FzhkgdBHevobVOcJniYQd4eDRnuLHiZ47y4vRLuEibZam7yOFfg+3+VaE//6DiCtc6h6/q8wPYo9lh6vnqgmiP2K2t2JfkBMPE8Yol/L+8KgtXZZ3njbsHlXQair5D+XYTGqAJpfiCntserBFIxlTlWitTs4AasbwNpbRpgpx5QsbgqX/d9qVWBaPfQ6riq1hjI5dNOByzUpD2A=="
//  ]
// }
// echo "Usage: register <user> <signer>"
class RegisterCmd(agent: Agent) : CmdBuilder<Agent>(agent, SsmCmdName.REGISTER)
