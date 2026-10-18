package com.gymmate.architecture;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Every endpoint declares the permission it needs (shared.security.Permission, granted per role
 * in RolePermissions), or is explicitly public. Role names in rules are not allowed: they
 * drifted out of step with the roles users actually hold.
 */
class EndpointPermissionTest {

    /** Endpoints without a method-level rule: public, webhooks, or authenticated by URL only. */
    private static final Set<String> NO_RULE = Set.of(
            "GET /", "GET /health",
            "POST /api/auth/login", "POST /api/auth/logout", "POST /api/auth/refresh",
            "POST /api/auth/register/owner", "POST /api/auth/register/member",
            "POST /api/auth/register/verify-otp", "POST /api/auth/register/resend-otp",
            "POST /api/auth/password-reset/request", "POST /api/auth/password-reset/confirm",
            "GET /api/auth/invite/validate", "POST /api/auth/invite/accept",
            "GET /api/auth/email-status/{userId}",
            "POST /api/webhooks/stripe/platform", "POST /api/webhooks/stripe/connect",
            "POST /api/webhooks/ses/events", "POST /api/webhooks/ses/unsubscribe", "GET /api/webhooks/ses/unsubscribe",
            "GET /api/subscriptions/tiers", "GET /api/subscriptions/tiers/featured",
            "GET /api/gyms/city/{city}");

    @Test
    void everyEndpointDeclaresAPermissionOrIsPublic() throws Exception {
        ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AnnotationTypeFilter(RestController.class));
        List<String> missing = new ArrayList<>();
        List<String> roleBased = new ArrayList<>();
        int endpoints = 0;
        for (BeanDefinition bd : scanner.findCandidateComponents("com.gymmate")) {
            Class<?> type = Class.forName(bd.getBeanClassName());
            RequestMapping base = AnnotatedElementUtils.findMergedAnnotation(type, RequestMapping.class);
            String prefix = base != null && base.path().length > 0 ? base.path()[0] : "";
            for (Method method : type.getDeclaredMethods()) {
                RequestMapping mapping = AnnotatedElementUtils.findMergedAnnotation(method, RequestMapping.class);
                if (mapping == null) continue;
                endpoints++;
                String path = (prefix + (mapping.path().length > 0 ? mapping.path()[0] : "")).replaceAll("/$", "");
                String key = mapping.method()[0].name() + " " + (path.isEmpty() ? "/" : path);
                PreAuthorize rule = method.getAnnotation(PreAuthorize.class);
                if (rule == null) {
                    if (!NO_RULE.contains(key)) missing.add(key);
                } else if (rule.value().contains("Role(")) {
                    roleBased.add(key + " -> " + rule.value());
                }
            }
        }
        assertThat(endpoints).isGreaterThan(300);
        assertThat(missing).as("endpoints without @PreAuthorize (add a permission or list them as public)").isEmpty();
        assertThat(roleBased).as("rules must use hasAuthority('<permission>'), not role names").isEmpty();
    }
}
