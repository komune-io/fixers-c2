package ssm.chaincode.dsl.config

import f2.dsl.fnc.operators.Batch
import f2.dsl.fnc.operators.BATCH_DEFAULT_SIZE
import f2.dsl.fnc.operators.BATCH_DEFAULT_CONCURRENCY
import kotlin.js.JsExport
import kotlinx.serialization.Serializable

/**
 * The configuration needed for a batch.
 * @d2 model
 * @parent [ssm.chaincode.dsl.SsmChaincodeD2Model]
 */
@JsExport
interface BatchPropertiesDTO {
    /**
     * The size of the batch.
     */
    val timeout: Int
    /**
     * The size of the batch.
     */
    val size: Int

    /**
     * The concurrency level of the batch.
     */
    val concurrency: Int
}

@JsExport
@Serializable
class SsmBatchProperties(
    override val timeout: Int = 2000,
    override val size: Int = BATCH_DEFAULT_SIZE,
    override val concurrency: Int = BATCH_DEFAULT_CONCURRENCY
) : BatchPropertiesDTO

/**
 * Converts an instance of [SsmBatchProperties] to a [Batch].
 *
 * @return a new [Batch] instance with the same size and concurrency.
 */
fun SsmBatchProperties.toBatch(): Batch = Batch(size, concurrency)
