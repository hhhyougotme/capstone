package com.flashmart.redis;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderStreamPublisher {

    private final RedisTemplate<String, String> streamRedisTemplate;
    private final StreamFeatureGate streamFeatureGate;

    public void publishOrderPlaced(long orderId, long userId, long flashSaleEventId, long merchantId) {
        if (!streamFeatureGate.isEnabled()) {
            return;
        }
        try {
            Map<String, String> body = Map.of(
                    "type", "ORDER_PLACED",
                    "orderId", Long.toString(orderId),
                    "userId", Long.toString(userId),
                    "flashSaleEventId", Long.toString(flashSaleEventId),
                    "merchantId", Long.toString(merchantId)
            );
            streamRedisTemplate.opsForStream().add(OrderStreamConsumer.STREAM_KEY, body);
        } catch (Exception ex) {
            log.warn("Failed to append order event to Redis Stream: {}", ex.getMessage());
            streamFeatureGate.disable();
        }
    }
}
