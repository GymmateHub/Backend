package com.gymmate.unit.notification.application;

import com.gymmate.notification.internal.application.EmailService;
import com.gymmate.notification.internal.application.EmailSuppressionService;
import com.gymmate.notification.internal.application.port.RealtimeNotifier;
import com.gymmate.notification.internal.application.port.SesConfigurationSetResolver;
import com.gymmate.notification.internal.application.port.SesTenantResolver;
import com.gymmate.whitelabel.api.WhitelabelApi;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;
import org.thymeleaf.TemplateEngine;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;

/**
 * The GymMateHub logo travels with transactional emails as an inline (CID) image, so it shows
 * without a hosted URL and in clients that block data: URIs.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("EmailService logo")
class EmailServiceLogoTest {

    @Mock
    private TemplateEngine templateEngine;
    @Mock
    private RealtimeNotifier realtimeNotifier;
    @Mock
    private WhitelabelApi whitelabelApi;
    @Mock
    private EmailSuppressionService suppressionService;
    @Mock
    private SesTenantResolver sesTenantResolver;
    @Mock
    private SesConfigurationSetResolver sesConfigurationSetResolver;
    @Mock
    private JavaMailSender mailSender;

    private EmailService emailService;

    @BeforeEach
    void setUp() {
        emailService = new EmailService(templateEngine, realtimeNotifier, whitelabelApi, suppressionService,
                sesTenantResolver, sesConfigurationSetResolver, mailSender);
        ReflectionTestUtils.setField(emailService, "defaultFromEmail", "noreply@gymmatehub.test");
        lenient().when(mailSender.createMimeMessage()).thenReturn(new MimeMessage((Session) null));
        lenient().when(whitelabelApi.findProfile(any(), any())).thenReturn(Optional.empty());
    }

    private String sentMessage() throws Exception {
        ArgumentCaptor<MimeMessage> captor = ArgumentCaptor.forClass(MimeMessage.class);
        verify(mailSender).send(captor.capture());
        ByteArrayOutputStream raw = new ByteArrayOutputStream();
        captor.getValue().writeTo(raw);
        return raw.toString(StandardCharsets.UTF_8);
    }

    @Test
    @DisplayName("attaches the logo inline when the email references it")
    void attachesLogoInline() throws Exception {
        emailService.sendHtmlEmail("ada@example.com", "Welcome",
                "<img src=\"cid:gymmatehub-logo\" alt=\"GymMateHub\"><p>Hi</p>");

        assertThat(sentMessage())
                .contains("Content-ID: <gymmatehub-logo>")
                .contains("Content-Type: image/png");
    }

    @Test
    @DisplayName("leaves emails that don't show the logo without it")
    void noLogoWhenNotReferenced() throws Exception {
        emailService.sendHtmlEmail("ada@example.com", "Campaign", "<p>No logo here</p>");

        assertThat(sentMessage()).doesNotContain("gymmatehub-logo");
    }

    @ParameterizedTest(name = "{0}")
    @ValueSource(strings = {"password-reset", "registration-otp", "subscription-expired", "subscription-renewal",
            "subscription-trial-ending", "welcome"})
    @DisplayName("every transactional template shows the logo")
    void templatesShowTheLogo(String template) throws Exception {
        String html = new ClassPathResource("templates/" + template + ".html").getContentAsString(StandardCharsets.UTF_8);

        assertThat(html).contains("src=\"cid:gymmatehub-logo\"").contains("alt=\"GymMateHub\"");
        assertThat(new ClassPathResource("email/gymmatehub-logo.png").exists()).isTrue();
    }
}
