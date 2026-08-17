package s2.sample.orderbook.sourcing.app.ssm

import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactive.awaitSingle
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.stereotype.Component
import s2.sample.subautomate.domain.model.OrderBook
import s2.sourcing.dsl.snap.SnapRepository

@Component
class RedisSnapView(
    private val template: ReactiveRedisTemplate<String, OrderBook>,
): SnapRepository<OrderBook, String>  {

    override suspend fun get(id: String): OrderBook? {
        return template.opsForValue().get(id).awaitFirstOrNull()
    }

    override suspend fun save(entity: OrderBook): OrderBook {
        template.opsForValue().set(entity.id, entity).awaitSingle()
        return entity
    }

    override suspend fun remove(id: String): Boolean {
        return template.delete(id).awaitSingle() > 0
    }
}
