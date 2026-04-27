@file:Suppress("MaxLineLength")

package ssm.sdk.core.invoke.command

import ssm.chaincode.dsl.model.SsmContextDTO
import ssm.sdk.core.invoke.builder.CmdBuilder
import ssm.sdk.dsl.SsmCmdName

// {
//    "InvokeArgs": [
//        "perform",
//        "Sell",
//        "{\"session\":\"deal20181201\",\"public\":\"100 dollars 1978 Camaro\",\"iteration\":0}",
//        "sam",
//        "ANOVTS1mqW2M+u7IoR/A/S3lY2xnj7yS8fJg0k0XE3DeY+i23LzJL1ABrm/CxHbndqVtvmsDQ0pLJ/XGmdAxhpTSAj+oIi+bnQAxW5fAqtLt9KHOElnG7KWzO8xitHh7NaIDgMbMjxJ5tj8xRFB2OD69P6aqtv9sj6TqkIhWNCMYPzDl+/Rck3Su7/51heDeTkDjtPxnkyOYTnSTJF7eFaMTyauqAqtjQwznL9xhKIlxMcmLxmboEDNQd8tv3mT/8ALGhmo1YUWtMkgJ00li3NDhjq1+gVNjAcUpBhwN/N8lUmN6MElc9qwliHVOsWkwHAYvZ7r6Zdvf6typbFqkeA=="
//    ]
// }
//    echo "Usage: perform <action> <context> <signer>"
class PerformCmd(performAction: String, context: SsmContextDTO) :
	CmdBuilder<SsmContextDTO?>(context, SsmCmdName.PERFORM, performAction)
