package com.gymmate.unit.lead;

import com.gymmate.lead.api.dto.LeadCreateRequest;
import com.gymmate.lead.application.LeadService;
import com.gymmate.lead.domain.Lead;
import com.gymmate.lead.domain.LeadStatus;
import com.gymmate.lead.infrastructure.LeadRepository;
import com.gymmate.shared.exception.DomainException;
import com.gymmate.shared.exception.ResourceNotFoundException;
import com.gymmate.shared.multitenancy.TenantContext;
import org.junit.jupiter.api.AfterEach;
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
class LeadServiceTest {

    @Mock private LeadRepository leadRepository;
    @InjectMocks private LeadService leadService;

    private final UUID orgId = UUID.randomUUID();
    private final UUID gymId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        TenantContext.setCurrentTenantId(orgId);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    private LeadCreateRequest request(UUID gym) {
        return new LeadCreateRequest(gym, "  Ada ", " Obi ", " ada@example.com ", "+2348011111111",
                "Instagram", "Wants a trial", null, null);
    }

    @Test
    void createsNewLeadTrimmedAndScopedToGymAndOrganisation() {
        when(leadRepository.save(any(Lead.class))).thenAnswer(inv -> inv.getArgument(0));

        Lead lead = leadService.createLead(gymId, request(gymId));

        assertEquals("Ada", lead.getFirstName());
        assertEquals("Obi", lead.getLastName());
        assertEquals("ada@example.com", lead.getEmail());
        assertEquals(LeadStatus.NEW, lead.getStatus());
        assertEquals(gymId, lead.getGymId());
        assertEquals(orgId, lead.getOrganisationId());
    }

    @Test
    void fallsBackToGymFromTenantContext() {
        TenantContext.setCurrentGymId(gymId);
        when(leadRepository.save(any(Lead.class))).thenAnswer(inv -> inv.getArgument(0));

        Lead lead = leadService.createLead(null, request(null));

        assertEquals(gymId, lead.getGymId());
    }

    @Test
    void rejectsLeadWithNoGymAsBadRequest() {
        DomainException ex = assertThrows(DomainException.class,
                () -> leadService.createLead(null, request(null)));

        assertEquals("GYM_ID_REQUIRED", ex.getErrorCode());
        verify(leadRepository, never()).save(any());
    }

    @Test
    void convertMarksLeadConvertedAndLinksMember() {
        UUID leadId = UUID.randomUUID();
        UUID memberId = UUID.randomUUID();
        Lead lead = Lead.builder().firstName("Ada").lastName("Obi").build();
        when(leadRepository.findById(leadId)).thenReturn(Optional.of(lead));
        when(leadRepository.save(any(Lead.class))).thenAnswer(inv -> inv.getArgument(0));

        Lead converted = leadService.convert(leadId, memberId);

        assertEquals(LeadStatus.CONVERTED, converted.getStatus());
        assertEquals(memberId, converted.getConvertedMemberId());
        assertNotNull(converted.getConvertedAt());
    }

    @Test
    void updateStatusToConvertedStampsConvertedAtOnce() {
        UUID leadId = UUID.randomUUID();
        Lead lead = Lead.builder().firstName("Ada").lastName("Obi").build();
        when(leadRepository.findById(leadId)).thenReturn(Optional.of(lead));
        when(leadRepository.save(any(Lead.class))).thenAnswer(inv -> inv.getArgument(0));

        Lead first = leadService.updateStatus(leadId, LeadStatus.CONVERTED);
        var stamp = first.getConvertedAt();
        Lead second = leadService.updateStatus(leadId, LeadStatus.CONVERTED);

        assertNotNull(stamp);
        assertEquals(stamp, second.getConvertedAt());
    }

    @Test
    void findByIdThrowsNotFound() {
        UUID id = UUID.randomUUID();
        when(leadRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> leadService.findById(id));
    }

    @Test
    void deleteRemovesTheLead() {
        UUID id = UUID.randomUUID();
        Lead lead = Lead.builder().firstName("Ada").lastName("Obi").build();
        when(leadRepository.findById(id)).thenReturn(Optional.of(lead));

        leadService.deleteLead(id);

        verify(leadRepository).delete(lead);
    }
}
