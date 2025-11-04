package com.gymmate.user.application;

import com.gymmate.shared.exception.DomainException;
import com.gymmate.shared.exception.ResourceNotFoundException;
import com.gymmate.user.domain.Member;
import com.gymmate.user.domain.MemberStatus;
import com.gymmate.user.domain.User;
import com.gymmate.user.infrastructure.MemberRepository;
import com.gymmate.user.infrastructure.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
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

    /**
     * Create a new member profile for an existing user.
     */
    @Transactional
    public Member createMember(UUID userId, String membershipNumber) {
        // Verify user exists
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId.toString()));

        // Check if member profile already exists
        if (memberRepository.existsByUserId(userId)) {
            throw new DomainException("MEMBER_ALREADY_EXISTS",
                    "Member profile already exists for user: " + userId);
        }

        // Check membership number uniqueness
        if (membershipNumber != null && memberRepository.existsByMembershipNumber(membershipNumber)) {
            throw new DomainException("MEMBERSHIP_NUMBER_EXISTS",
                    "Membership number already exists: " + membershipNumber);
        }

        // Create member
        Member member = Member.builder()
                .userId(userId)
                .membershipNumber(membershipNumber)
                .joinDate(LocalDate.now())
                .status(MemberStatus.ACTIVE)
                .waiverSigned(false)
                .photoConsent(false)
                .build();

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
        return memberRepository.save(member);
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
     * Count members by status.
     */
    public long countByStatus(MemberStatus status) {
        return memberRepository.countByStatus(status);
    }

    /**
     * Find new members joined after a date.
     */
    public List<Member> findNewMembers(LocalDate afterDate) {
        return memberRepository.findByJoinDateAfter(afterDate);
    }

    /**
     * Find all members.
     */
    public List<Member> findAll() {
        return memberRepository.findAll();
    }
}

