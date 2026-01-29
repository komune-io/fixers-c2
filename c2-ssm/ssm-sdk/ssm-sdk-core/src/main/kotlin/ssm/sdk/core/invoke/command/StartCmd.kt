@file:Suppress("MaxLineLength")

package ssm.sdk.core.invoke.command

import ssm.chaincode.dsl.model.SsmSession
import ssm.sdk.core.invoke.builder.CmdBuilder
import ssm.sdk.dsl.SsmCmdName

// {
//    "InvokeArgs": [
//    "start",
//    "{\"ssm\":\"Car dealership\",\"session\":\"deal20181201\",\"public\":\"Used car for 100 dollars.\",\"roles\":{\"bob\":\"Buyer\",\"sam\":\"Seller\"}}",
//    "adam",
//    "RSjlvcSl8PnwlWuYgP3c+uCF1qUEimsyUBjwtWNyQHIVOq3W1cXkzTvfB0bJ3HUSjT2fq00JRLlWQ+BeTktPJjEoF2tn2Xmpd3LP7iHDVgtQzDosTfi30fMWwjyM8IcL0YXNDr+n8U+gxLpYcs33MuPXtVEqakJot+mKI8uDr5vT8H6G7fFMix1UVfLRuPER0wExMxzqk75DBBf2qFYJ2wmhp+QSeiiOf2GdujnP+HfDBZDVC8LdjwjHSuX22sSzGiDhI63IOE42MpuV7iD5L/zjzcdktfbDlzj4r5MR8x6AjkkYFK29KsCmC8nSkZ7oGNGvvEVixd8fO8sXh3aQdg=="
//    ]
// }
// {
//    "ssm":"Car dealership",
//    "session":"deal20181201",
//    "public":"Used car for 100 dollars.",
//    "roles":{
//      "bob":"Buyer",
//       "sam":"Seller"
//    }
// }
// echo "Usage: start <session> <signer>"
class StartCmd(session: SsmSession) : CmdBuilder<SsmSession>(session,  SsmCmdName.START)
