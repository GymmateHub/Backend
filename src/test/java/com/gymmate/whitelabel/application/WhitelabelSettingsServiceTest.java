package com.gymmate.whitelabel.application;

import com.gymmate.whitelabel.api.dto.WhitelabelSettingsRequest;
import com.gymmate.whitelabel.api.dto.WhitelabelSettingsResponse;
import com.gymmate.whitelabel.domain.SmtpSecurity;
import com.gymmate.whitelabel.domain.WhitelabelSettings;
import com.gymmate.whitelabel.infrastructure.WhitelabelSettingsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WhitelabelSettingsServiceTest {

    @Mock
    private WhitelabelSettingsRepository settingsRepository;

    @Mock
    private WhitelabelEncryptionService encryptionService;

    @Mock
    private DynamicMailSenderFactory mailSenderFactory;

    @InjectMocks
    private WhitelabelSettingsService settingsService;

    private UUID organisationId;
    private UUID gymId;

    @BeforeEach
    void setUp() {
        organisationId = UUID.randomUUID();
        gymId = UUID.randomUUID();
    }

    @Test
    void testSaveOrganisationSettingsEncryptsPassword() {
        WhitelabelSettingsRequest request = WhitelabelSettingsRequest.builder()
                .brandName("FitNation")
                .smtpEnabled(true)
                .smtpHost("smtp.fitnation.com")
                .smtpPort(587)
                .smtpUsername("admin@fitnation.com")
                .smtpPassword("SecretPass123!")
                .smtpSecurity(SmtpSecurity.STARTTLS)
                .build();

        when(settingsRepository.findByOrganisationIdAndGymIdIsNull(organisationId))
                .thenReturn(Optional.empty());
        when(encryptionService.encrypt("SecretPass123!"))
                .thenReturn("ENCRYPTED_PASS");
        when(settingsRepository.save(any(WhitelabelSettings.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        WhitelabelSettingsResponse response = settingsService.saveOrganisationSettings(organisationId, request);

        assertNotNull(response);
        assertEquals("FitNation", response.getBrandName());
        assertTrue(response.isSmtpEnabled());
        assertEquals("smtp.fitnation.com", response.getSmtpHost());
        assertEquals("••••••••", response.getSmtpPasswordMasked());

        verify(encryptionService).encrypt("SecretPass123!");
        verify(mailSenderFactory).evictCache(organisationId, null);
    }

    @Test
    void testGetWhitelabelSettingsFallbackToOrg() {
        WhitelabelSettings orgSettings = WhitelabelSettings.builder()
                .brandName("OrgLevelBrand")
                .build();
        orgSettings.setOrganisationId(organisationId);

        when(settingsRepository.findByOrganisationIdAndGymId(organisationId, gymId))
                .thenReturn(Optional.empty());
        when(settingsRepository.findByOrganisationIdAndGymIdIsNull(organisationId))
                .thenReturn(Optional.of(orgSettings));

        Optional<WhitelabelSettings> resolved = settingsService.getWhitelabelSettings(organisationId, gymId);

        assertTrue(resolved.isPresent());
        assertEquals("OrgLevelBrand", resolved.get().getBrandName());
    }
}
