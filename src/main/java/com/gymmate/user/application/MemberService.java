package com.gymmate.user.application;

import com.gymmate.shared.constants.UserRole;
import com.gymmate.shared.constants.UserStatus;
import com.gymmate.shared.exception.DomainException;
import com.gymmate.shared.exception.ResourceNotFoundException;
import com.gymmate.shared.multitenancy.TenantContext;
import com.gymmate.user.domain.Member;
import com.gymmate.shared.constants.MemberStatus;
import com.gymmate.user.domain.User;
import com.gymmate.user.infrastructure.MemberRepository;
import com.gymmate.user.infrastructure.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Application service for member management use cases.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final org.springframework.context.ApplicationEventPublisher eventPublisher;

    /**
     * Create a new member profile for an existing user or create user if needed.
     */
    @Transactional
    public Member createMemberWithDetails(String email, String firstName, String lastName, String phone,
                                          UUID gymId, String membershipNumber) {
        UUID orgId = TenantContext.getCurrentTenantId();

        if (email == null || email.isBlank()) {
            throw new DomainException("EMAIL_REQUIRED", "Email is required to create a member");
        }
        if (firstName == null || firstName.isBlank()) {
            throw new DomainException("FIRST_NAME_REQUIRED", "First name is required to create a member");
        }
        if (lastName == null || lastName.isBlank()) {
            throw new DomainException("LAST_NAME_REQUIRED", "Last name is required to create a member");
        }

        User user = userRepository.findByEmail(email.trim()).orElseGet(() -> {
            User newUser = User.builder()
                    .email(email.trim())
                    .firstName(firstName.trim())
                    .lastName(lastName.trim())
                    .phone(phone != null ? phone.trim() : null)
                    .passwordHash(passwordEncoder.encode(UUID.randomUUID().toString()))
                    .role(UserRole.MEMBER)
                    .status(UserStatus.ACTIVE)
                    .emailVerified(true)
                    .build();
            newUser.setOrganisationId(orgId);
            return userRepository.save(newUser);
        });

        // Generate membership number if none provided
        String memberNo = membershipNumber;
        if (memberNo == null || memberNo.isBlank()) {
            memberNo = "GM-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        }

        // If member profile already exists for this user, return it
        Optional<Member> existing = memberRepository.findByUserId(user.getId());
        if (existing.isPresent()) {
            return existing.get();
        }

        UUID targetGymId = gymId;

        Member member = Member.builder()
                .userId(user.getId())
                .membershipNumber(memberNo)
                .joinDate(LocalDate.now())
                .status(MemberStatus.ACTIVE)
                .waiverSigned(false)
                .photoConsent(false)
                .build();

        member.setGymId(targetGymId);
        member.setOrganisationId(user.getOrganisationId() != null ? user.getOrganisationId() : orgId);

        return memberRepository.save(member);
    }

    /**
     * Create a new member profile for an existing user.
     */
    @Transactional
    public Member createMember(UUID userId, UUID gymId, String membershipNumber) {
        // Verify user exists
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId.toString()));

        // Verify gymId is provided
        if (gymId == null) {
            throw new DomainException("GYM_ID_REQUIRED",
                    "Gym ID is required when creating a member");
        }

        // Check if member profile already exists
        if (memberRepository.existsByUserId(userId)) {
            throw new DomainException("MEMBER_ALREADY_EXISTS",
                    "Member profile already exists for user: " + userId);
        }

        String memberNo = membershipNumber;
        if (memberNo == null || memberNo.isBlank()) {
            memberNo = "GM-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        }

        // Create member
        Member member = Member.builder()
                .userId(userId)
                .membershipNumber(memberNo)
                .joinDate(LocalDate.now())
                .status(MemberStatus.ACTIVE)
                .waiverSigned(false)
                .photoConsent(false)
                .build();

        member.setGymId(gymId);
        member.setOrganisationId(user.getOrganisationId() != null
                ? user.getOrganisationId()
                : TenantContext.getCurrentTenantId());

        return memberRepository.save(member);
    }

    /**
     * Update emergency contact information.
     */
    @Transactional
    public Member updateEmergencyContact(UUID memberId, String name, String phone, String relationship) {
        Member member = findById(memberId);
        member.updateEmergencyContact(name, phone, relationship);
        return memberRepository.save(member);
    }

    /**
     * Sign waiver for member.
     */
    @Transactional
    public Member signWaiver(UUID memberId) {
        Member member = findById(memberId);
        member.signWaiver();
        return memberRepository.save(member);
    }

    /**
     * Update member health information.
     */
    @Transactional
    public Member updateHealthInfo(UUID memberId, String[] medicalConditions,
                                   String[] allergies, String[] medications) {
        Member member = findById(memberId);
        member.setMedicalConditions(medicalConditions);
        member.setAllergies(allergies);
        member.setMedications(medications);
        return memberRepository.save(member);
    }

    /**
     * Update fitness goals.
     */
    @Transactional
    public Member updateFitnessGoals(UUID memberId, String[] fitnessGoals, String experienceLevel) {
        Member member = findById(memberId);
        member.setFitnessGoals(fitnessGoals);
        member.setExperienceLevel(experienceLevel);
        Member savedMember = memberRepository.save(member);
        
        // Trigger AI plan generation if fitness goals are provided
        if (fitnessGoals != null && fitnessGoals.length > 0) {
            eventPublisher.publishEvent(new com.gymmate.user.domain.events.MemberOnboardedEvent(this, savedMember.getOrganisationId(), savedMember.getId(), savedMember.getGymId(), fitnessGoals));
        }
        
        return savedMember;
    }

    /**
     * Activate member.
     */
    @Transactional
    public Member activate(UUID memberId) {
        Member member = findById(memberId);
        member.activate();
        return memberRepository.save(member);
    }

    /**
     * Suspend member.
     */
    @Transactional
    public Member suspend(UUID memberId) {
        Member member = findById(memberId);
        member.suspend();
        return memberRepository.save(member);
    }

    /**
     * Cancel member.
     */
    @Transactional
    public Member cancel(UUID memberId) {
        Member member = findById(memberId);
        member.cancel();
        return memberRepository.save(member);
    }

    /**
     * Find member by ID.
     */
    public Member findById(UUID id) {
        return memberRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Member", id.toString()));
    }

    /**
     * Find member by user ID.
     */
    public Member findByUserId(UUID userId) {
        return memberRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Member", "userId=" + userId));
    }

    /**
     * Find member by membership number.
     */
    public Member findByMembershipNumber(String membershipNumber) {
        return memberRepository.findByMembershipNumber(membershipNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Member", "membershipNumber=" + membershipNumber));
    }

    /**
     * Find members by status.
     */
    public List<Member> findByStatus(MemberStatus status) {
        return memberRepository.findByStatus(status);
    }

    /**
     * Find all active members.
     */
    public List<Member> findActiveMembers() {
        return memberRepository.findByStatus(MemberStatus.ACTIVE);
    }

    /**
     * Find members who haven't signed waiver.
     */
    public List<Member> findMembersWithoutWaiver() {
        return memberRepository.findByWaiverSignedFalse();
    }

    /**
     * Count members by status within an organisation.
     */
    public long countByStatus(UUID organisationId, MemberStatus status) {
        return memberRepository.countByOrganisationIdAndStatus(organisationId, status);
    }

    /**
     * Find new members joined after a date within an organisation.
     */
    public List<Member> findNewMembers(UUID organisationId, LocalDate afterDate) {
        return memberRepository.findByOrganisationIdAndJoinDateAfter(organisationId, afterDate);
    }

    /**
     * Find all members.
     */
    public List<Member> findAll() {
        return memberRepository.findAll();
    }
}
