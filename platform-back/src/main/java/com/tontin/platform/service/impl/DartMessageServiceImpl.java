package com.tontin.platform.service.impl;

import com.tontin.platform.config.SecurityUtils;
import com.tontin.platform.domain.Dart;
import com.tontin.platform.domain.DartMessage;
import com.tontin.platform.domain.Member;
import com.tontin.platform.domain.enums.member.MemberStatus;
import com.tontin.platform.dto.dart.request.CreateMessageRequest;
import com.tontin.platform.domain.DartMessageReaction;
import com.tontin.platform.dto.dart.response.MessageResponse;
import com.tontin.platform.dto.dart.response.MessageResponse.ReactionSummary;
import com.tontin.platform.dto.dart.response.PageResponse;
import com.tontin.platform.repository.DartMessageReactionRepository;
import com.tontin.platform.repository.DartMessageRepository;
import com.tontin.platform.repository.DartRepository;
import com.tontin.platform.repository.MemberRepository;
import com.tontin.platform.service.DartMessageService;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Slf4j
@RequiredArgsConstructor
public class DartMessageServiceImpl implements DartMessageService {

    private final DartMessageRepository messageRepository;
    private final DartMessageReactionRepository reactionRepository;
    private final DartRepository dartRepository;
    private final MemberRepository memberRepository;
    private final SecurityUtils securityUtils;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<MessageResponse> getMessages(UUID dartId, int page, int size) {
        UUID userId = securityUtils.requireCurrentUserId();
        ensureMemberOfDart(dartId, userId);

        Dart dart = dartRepository
            .findById(dartId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Dart not found: " + dartId
            ));

        Pageable pageable = PageRequest.of(page, size);
        Page<DartMessage> messagePage = messageRepository.findByDartIdOrderByCreatedAtAsc(
            dartId,
            pageable
        );

        List<UUID> messageIds = messagePage.getContent().stream()
            .map(DartMessage::getId)
            .toList();
        Map<UUID, List<ReactionSummary>> reactionsByMessage = messageIds.isEmpty()
            ? Map.of()
            : buildReactionsByMessage(messageIds, dartId, userId);

        Page<MessageResponse> responsePage = messagePage.map(m ->
            toResponse(m, reactionsByMessage.getOrDefault(m.getId(), List.of()))
        );
        return PageResponse.of(responsePage);
    }

    @Override
    @Transactional
    public MessageResponse createMessage(UUID dartId, CreateMessageRequest request) {
        UUID userId = securityUtils.requireCurrentUserId();
        Member sender = memberRepository
            .findByDartIdAndUserId(dartId, userId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "You are not a member of this dart"
            ));

        if (sender.getStatus() != MemberStatus.ACTIVE) {
            throw new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "Only active members can send messages"
            );
        }

        String content = request.content() != null ? request.content().trim() : "";
        if (content.isEmpty()) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Message content is required"
            );
        }

        Dart dart = dartRepository
            .findById(dartId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Dart not found: " + dartId
            ));

        DartMessage message = DartMessage
            .builder()
            .dart(dart)
            .sender(sender)
            .content(content)
            .build();

        message = messageRepository.save(message);
        log.info("Message created in dart {} by user {}", dartId, userId);
        // Re-fetch with sender and user loaded to avoid lazy init during serialization
        message = messageRepository
            .findByIdWithSenderAndUser(message.getId())
            .orElse(message);
        List<ReactionSummary> reactions = buildReactionsForMessage(message.getId(), dartId, userId);
        return toResponse(message, reactions);
    }

    private void ensureMemberOfDart(UUID dartId, UUID userId) {
        if (!memberRepository.existsByUserIdAndDartId(userId, dartId)) {
            throw new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "You are not a member of this dart"
            );
        }
    }

    private MessageResponse toResponse(DartMessage m, List<ReactionSummary> reactions) {
        Member sender = m.getSender();
        return MessageResponse
            .builder()
            .id(m.getId())
            .dartId(m.getDart().getId())
            .userId(sender.getUser().getId())
            .userName(sender.getUser().getUserName())
            .content(m.getContent() != null ? m.getContent() : "")
            .createdAt(m.getCreatedAt())
            .reactions(reactions != null ? reactions : List.of())
            .build();
    }

    private Map<UUID, List<ReactionSummary>> buildReactionsByMessage(
        List<UUID> messageIds,
        UUID dartId,
        UUID currentUserId
    ) {
        List<DartMessageReaction> all = reactionRepository.findByMessageIdIn(messageIds);
        UUID currentMemberId = memberRepository
            .findByDartIdAndUserId(dartId, currentUserId)
            .map(Member::getId)
            .orElse(null);

        return all.stream()
            .collect(Collectors.groupingBy(r -> r.getMessage().getId()))
            .entrySet()
            .stream()
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                e -> aggregateReactions(e.getValue(), currentMemberId)
            ));
    }

    private List<ReactionSummary> buildReactionsForMessage(UUID messageId, UUID dartId, UUID currentUserId) {
        List<DartMessageReaction> list = reactionRepository.findByMessageIdWithMemberUser(messageId);
        UUID currentMemberId = memberRepository
            .findByDartIdAndUserId(dartId, currentUserId)
            .map(Member::getId)
            .orElse(null);
        return aggregateReactions(list, currentMemberId);
    }

    private List<ReactionSummary> aggregateReactions(List<DartMessageReaction> list, UUID currentMemberId) {
        if (list == null || list.isEmpty()) return List.of();
        return list.stream()
            .collect(Collectors.groupingBy(DartMessageReaction::getEmoji))
            .entrySet()
            .stream()
            .map(entry -> {
                String emoji = entry.getKey();
                List<DartMessageReaction> reactions = entry.getValue();
                long count = reactions.size();
                boolean reactedByCurrentUser = currentMemberId != null && reactions.stream()
                    .anyMatch(r -> Objects.equals(r.getMember().getId(), currentMemberId));
                List<String> names = reactions.stream()
                    .map(r -> r.getMember().getUser().getUserName())
                    .filter(Objects::nonNull)
                    .toList();
                return new ReactionSummary(emoji, count, reactedByCurrentUser, names);
            })
            .toList();
    }

    @Override
    @Transactional
    public void addReaction(UUID dartId, UUID messageId, String emoji) {
        UUID userId = securityUtils.requireCurrentUserId();
        ensureMemberOfDart(dartId, userId);
        Member member = memberRepository
            .findByDartIdAndUserId(dartId, userId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "You are not a member of this dart"
            ));
        DartMessage message = messageRepository
            .findById(messageId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Message not found: " + messageId
            ));
        if (!message.getDart().getId().equals(dartId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Message not found in this dart");
        }
        if (reactionRepository.findByMessageIdAndMemberIdAndEmoji(messageId, member.getId(), emoji).isPresent()) {
            return; // idempotent
        }
        DartMessageReaction reaction = DartMessageReaction
            .builder()
            .message(message)
            .member(member)
            .emoji(emoji.trim())
            .build();
        reactionRepository.save(reaction);
    }

    @Override
    @Transactional
    public void removeReaction(UUID dartId, UUID messageId, String emoji) {
        UUID userId = securityUtils.requireCurrentUserId();
        Member member = memberRepository
            .findByDartIdAndUserId(dartId, userId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "You are not a member of this dart"
            ));
        DartMessage message = messageRepository
            .findById(messageId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Message not found: " + messageId
            ));
        if (!message.getDart().getId().equals(dartId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Message not found in this dart");
        }
        reactionRepository.deleteByMessageIdAndMemberIdAndEmoji(messageId, member.getId(), emoji.trim());
    }
}
