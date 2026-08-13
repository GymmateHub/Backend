package com.gymmate.user.infrastructure;

import com.gymmate.notification.application.AudienceResolver.MemberRecipient;
import com.gymmate.notification.application.port.MemberDirectory;
import com.gymmate.shared.constants.MemberStatus;
import com.gymmate.user.domain.Member;
import com.gymmate.user.domain.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implements notification's {@link MemberDirectory} port using this module's own
 * {@code Member}/{@code User} data — moved out of
 * {@code notification.application.AudienceResolver} to break a module dependency
 * cycle (see the port package Javadoc).
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class UserMemberDirectoryAdapter implements MemberDirectory {

    private final MemberRepository memberRepository;
    private final UserRepository userRepository;

    @Override
    public List<MemberRecipient> findActiveMembersByGym(UUID gymId) {
        List<Member> members = memberRepository.findByGymId(gymId).stream()
                .filter(m -> m.getStatus() == MemberStatus.ACTIVE)
                .collect(Collectors.toList());
        return enrich(members);
    }

    @Override
    public List<MemberRecipient> findMembersByIds(UUID gymId, Set<UUID> memberIds, boolean activeOnly) {
        if (memberIds.isEmpty()) {
            return Collections.emptyList();
        }
        List<Member> members = memberRepository.findAllById(memberIds).stream()
                .filter(m -> m.getGymId().equals(gymId))
                .filter(m -> !activeOnly || m.getStatus() == MemberStatus.ACTIVE)
                .collect(Collectors.toList());
        return enrich(members);
    }

    private List<MemberRecipient> enrich(List<Member> members) {
        if (members.isEmpty()) {
            return Collections.emptyList();
        }

        Set<UUID> userIds = members.stream()
                .map(Member::getUserId)
                .collect(Collectors.toSet());

        List<User> users = userRepository.findAllById(userIds);
        Map<UUID, User> userMap = users.stream()
                .collect(Collectors.toMap(User::getId, u -> u));

        return members.stream()
                .map(member -> {
                    User user = userMap.get(member.getUserId());
                    if (user == null) {
                        log.warn("User not found for member: {}", member.getId());
                        return null;
                    }
                    return new MemberRecipient(
                            member.getId(),
                            user.getId(),
                            user.getFirstName(),
                            user.getLastName(),
                            user.getEmail(),
                            user.getPhone());
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
}
