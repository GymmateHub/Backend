package com.gymmate.billing.internal.infrastructure.web;

import com.gymmate.billing.internal.application.RateLimitService;
import com.gymmate.shared.multitenancy.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Parallel requests of one organisation update the same rate-limit window row, so the
 * limiter's bookkeeping can fail (optimistic locking). That must not fail the request itself.
 */
class RateLimitInterceptorTest {

    private final RateLimitService rateLimitService = mock(RateLimitService.class);
    private final RateLimitInterceptor interceptor = new RateLimitInterceptor(rateLimitService);

    @BeforeEach
    void tenant() {
        TenantContext.setCurrentTenantId(UUID.randomUUID());
    }

    @AfterEach
    void clear() {
        TenantContext.clear();
    }

    @Test
    void bookkeepingConflictLetsTheRequestThrough() throws Exception {
        when(rateLimitService.checkRateLimit(any(), anyString(), anyString(), any()))
                .thenThrow(new OptimisticLockingFailureException("row updated by a parallel request"));
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean proceed = interceptor.preHandle(new MockHttpServletRequest("GET", "/api/gyms"), response, new Object());

        assertThat(proceed).isTrue();
        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    void exceededLimitStillReturns429() throws Exception {
        when(rateLimitService.checkRateLimit(any(), anyString(), anyString(), any())).thenReturn(false);
        when(rateLimitService.getRateLimitStatus(any())).thenReturn(com.gymmate.billing.internal.application.RateLimitStatus.builder()
                .tierName("starter").hourlyLimit(1000).hourlyRemaining(0).burstLimit(100).burstRemaining(0).build());
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean proceed = interceptor.preHandle(new MockHttpServletRequest("GET", "/api/gyms"), response, new Object());

        assertThat(proceed).isFalse();
        assertThat(response.getStatus()).isEqualTo(429);
    }
}
