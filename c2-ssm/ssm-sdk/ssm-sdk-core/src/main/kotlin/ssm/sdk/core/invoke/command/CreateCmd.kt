@file:Suppress("MaxLineLength")

package ssm.sdk.core.invoke.command

import ssm.chaincode.dsl.model.Ssm
import ssm.sdk.core.invoke.builder.CmdBuilder
import ssm.sdk.dsl.SsmCmdName

//
// {
//    "InvokeArgs": [
//       "create",
//        "{\"name\":\"Car dealership\",\"transitions\":[{\"from\":0,\"to\":1,\"role\":\"Seller\",\"action\":\"Sell\"},{\"from\":1,\"to\":2,\"role\":\"Buyer\",\"action\":\"Buy\"}]}",
//        "adam",
//        "HUYPNHkgCfB+yr7TeYpi1dcU8me+MzPqFxtxJWBeIunBo/KHuG7/bS32MakwwDf7ehyIWDuXF42b/IT9RofKLU6P5DwpadDxE6cj1qlcIgRd1K015D9wvKFdJW9SfYTJhINwuitFhus/eNLcGb+CdyoyD0GRrYRONJ8C6/Hop2PwyCZ6v5aya+XxEoh+2EjPkdeDn0VbdXR5wGP7emI4R9ZhAHwp3ebHV139OdSvvGobllN9hUZdKBkF2nYinti/YfrBI9mfY4svPCg1zZfK0hfegAa8Rekysno/2+d9jkJMwCveTzclMpSFGlVO3mRr4yWQOIEre7VpaxfGx8zdow=="
//    ]
// }
// {
//  "name":"Car dealership",
//  "transitions":[
//    {
//      "from":0,
//      "to":1,
//      "role":"Seller",
//      "action":"Sell"
//    },
//    {
//      "from":1,
//       "to":2,
//       "role":"Buyer",
//       "action":"Buy"
//     }
//  ]
// }
//
// echo "Usage: create <command> <signer>"
class CreateCmd(ssm: Ssm) : CmdBuilder<Ssm>(ssm, SsmCmdName.CREATE)
