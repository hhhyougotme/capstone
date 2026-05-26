package com.flashmart.service;

import com.flashmart.config.AuthProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SessionService {

    private final StringRedisTemplate stringRedisTemplate;
    private final AuthProperties authProperties;

    public String createSession(Long userId) {
        String token = UUID.randomUUID().toString().replace("-", "");
        String key = authProperties.getRedisKeyPrefix() + token;
        stringRedisTemplate.opsForValue().set(
                key,
                String.valueOf(userId),
                Duration.ofHours(authProperties.getTokenTtlHours()));
        return token;
    }

    public Optional<Long> findUserId(String bearerOrRawToken) {
        if (bearerOrRawToken == null || bearerOrRawToken.isBlank()) {
            return Optional.empty();
        }
        String token = stripBearer(bearerOrRawToken.trim());
        String key = authProperties.getRedisKeyPrefix() + token;
        String v = stringRedisTemplate.opsForValue().get(key);
        if (v == null) {
            return Optional.empty();
        }
        try {
            return Optional.of(Long.parseLong(v));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    private String stripBearer(String header) {
        String p = authProperties.getTokenPrefix();
        if (p != null && header.regionMatches(true, 0, p.trim(), 0, p.trim().length())) {
            return header.substring(p.trim().length()).trim();
        }
        return header;
    }
}
