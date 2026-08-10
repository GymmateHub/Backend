package com.gymmate.shared.config;

import org.junit.jupiter.api.Test;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class RedissonConfigTest {

    @Test
    void redissonClientFallsBackGracefullyWhenRedisUnreachable() throws Exception {
        RedissonConfig config = new RedissonConfig();
        ReflectionTestUtils.setField(config, "redisHost", "127.0.0.1");
        ReflectionTestUtils.setField(config, "redisPort", 63899); // Unused port

        RedissonClient client = config.redissonClient();
        assertNotNull(client, "RedissonClient should not be null");

        RLock lock = client.getLock("test-lock");
        assertNotNull(lock, "Lock should not be null");
        assertTrue(lock.tryLock(1, TimeUnit.SECONDS), "Fallback lock tryLock should return true");
        assertTrue(lock.isHeldByCurrentThread(), "Fallback lock isHeldByCurrentThread should return true");
        assertDoesNotThrow(() -> lock.unlock());
        assertDoesNotThrow(() -> client.shutdown());
    }
}
