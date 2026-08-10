package com.gymmate.shared.config;

import lombok.extern.slf4j.Slf4j;
import org.redisson.Redisson;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.lang.reflect.Proxy;

/**
 * Redisson client wired to the same Redis instance already configured for
 * spring.data.redis (see application.yml) — used as a distributed-lock contention
 * optimiser (e.g. ClassBookingService's seat-reservation lock), not as the
 * correctness guarantee. The DB constraint/trigger is the correctness guarantee;
 * this only cuts down on rollback churn under contention.
 */
@Slf4j
@Configuration
public class RedissonConfig {

    @Value("${spring.data.redis.host:localhost}")
    private String redisHost;

    @Value("${spring.data.redis.port:6379}")
    private int redisPort;

    @Value("${spring.data.redis.password:}")
    private String redisPassword;

    @Bean(destroyMethod = "shutdown")
    public RedissonClient redissonClient() {
        Config config = new Config();
        String address = "redis://" + redisHost + ":" + redisPort;
        var serverConfig = config.useSingleServer()
                .setAddress(address)
                .setConnectTimeout(2000)
                .setTimeout(3000);
        if (redisPassword != null && !redisPassword.isBlank()) {
            serverConfig.setPassword(redisPassword);
        }
        try {
            return Redisson.create(config);
        } catch (Exception e) {
            log.warn("Unable to connect to Redis server at {}. Falling back to no-op RedissonClient for distributed locking. Error: {}", address, e.getMessage());
            return createFallbackRedissonClient();
        }
    }

    private RedissonClient createFallbackRedissonClient() {
        RLock fallbackLock = (RLock) Proxy.newProxyInstance(
                RLock.class.getClassLoader(),
                new Class<?>[]{RLock.class},
                (proxy, method, args) -> {
                    String methodName = method.getName();
                    if ("tryLock".equals(methodName)) {
                        return true;
                    }
                    if ("isHeldByCurrentThread".equals(methodName)) {
                        return true;
                    }
                    if ("unlock".equals(methodName)) {
                        return null;
                    }
                    if ("isLocked".equals(methodName)) {
                        return true;
                    }
                    Class<?> returnType = method.getReturnType();
                    if (returnType.equals(boolean.class) || returnType.equals(Boolean.class)) {
                        return true;
                    }
                    if (returnType.equals(long.class) || returnType.equals(Long.class)) {
                        return 0L;
                    }
                    if (returnType.equals(int.class) || returnType.equals(Integer.class)) {
                        return 0;
                    }
                    return null;
                }
        );

        return (RedissonClient) Proxy.newProxyInstance(
                RedissonClient.class.getClassLoader(),
                new Class<?>[]{RedissonClient.class},
                (proxy, method, args) -> {
                    String methodName = method.getName();
                    if ("getLock".equals(methodName)) {
                        return fallbackLock;
                    }
                    if ("shutdown".equals(methodName)) {
                        return null;
                    }
                    if ("isShutdown".equals(methodName) || "isTerminated".equals(methodName)) {
                        return false;
                    }
                    Class<?> returnType = method.getReturnType();
                    if (returnType.equals(boolean.class) || returnType.equals(Boolean.class)) {
                        return false;
                    }
                    return null;
                }
        );
    }
}

