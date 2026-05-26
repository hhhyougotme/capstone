package com.flashmart.redis;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.connection.stream.StreamReadOptions;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.time.Duration;
import java.util.List;

/**
 * Consumes order placement events from Redis Streams. Disabled when Redis does not support Streams.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OrderStreamConsumer {

    public static final String STREAM_KEY = "flashmart:stream:order-events";
    public static final String CONSUMER_GROUP = "flashmart-order-group";

    private final RedisTemplate<String, String> streamRedisTemplate;
    private final StreamFeatureGate streamFeatureGate;

    private final String consumerName = safeHost();

    private static String safeHost() {
        try {
            return InetAddress.getLocalHost().getHostName() + "-" + ProcessHandle.current().pid();
        } catch (Exception e) {
            return "flashmart-consumer-" + System.currentTimeMillis();
        }
    }

    @Scheduled(fixedDelayString = "${flashmart.streams.poll-ms:800}")
    public void consumeBatch() {
        if (!streamFeatureGate.isEnabled()) {
            return;
        }
        try {
            List<MapRecord<String, Object, Object>> records = streamRedisTemplate.opsForStream().read(
                    Consumer.from(CONSUMER_GROUP, consumerName),
                    StreamReadOptions.empty().count(32).block(Duration.ofMillis(600)),
                    StreamOffset.create(STREAM_KEY, ReadOffset.lastConsumed())
            );
            if (records == null || records.isEmpty()) {
                return;
            }
            for (MapRecord<String, Object, Object> rec : records) {
                log.info("Order stream event id={} body={}", rec.getId(), rec.getValue());
                streamRedisTemplate.opsForStream().acknowledge(CONSUMER_GROUP, rec);
            }
        } catch (Exception ex) {
            log.debug("Stream poll: {}", ex.getMessage());
            if (isStreamsUnsupported(ex)) {
                streamFeatureGate.disable();
            }
        }
    }

    private static boolean isStreamsUnsupported(Throwable ex) {
        for (int i = 0; i < 4 && ex != null; i++) {
            String m = ex.getMessage();
            if (m != null && m.contains("unknown command")) {
                return true;
            }
            ex = ex.getCause();
        }
        return false;
    }
}
