package com.gamegoo.gamegoo_v2.chat.service;

import com.gamegoo.gamegoo_v2.account.member.domain.Member;
import com.gamegoo.gamegoo_v2.account.member.repository.MemberRepository;
import com.gamegoo.gamegoo_v2.chat.domain.Chat;
import com.gamegoo.gamegoo_v2.chat.domain.Chatroom;
import com.gamegoo.gamegoo_v2.chat.domain.MemberChatroom;
import com.gamegoo.gamegoo_v2.chat.domain.SystemMessageType;
import com.gamegoo.gamegoo_v2.chat.dto.request.SystemFlagRequest;
import com.gamegoo.gamegoo_v2.chat.repository.ChatRepository;
import com.gamegoo.gamegoo_v2.chat.repository.ChatroomRepository;
import com.gamegoo.gamegoo_v2.chat.repository.MemberChatroomRepository;
import com.gamegoo.gamegoo_v2.content.board.domain.Board;
import com.gamegoo.gamegoo_v2.content.board.repository.BoardRepository;
import com.gamegoo.gamegoo_v2.core.common.validator.BlockValidator;
import com.gamegoo.gamegoo_v2.core.common.validator.MemberValidator;
import com.gamegoo.gamegoo_v2.core.event.SocketJoinEvent;
import com.gamegoo.gamegoo_v2.core.exception.ChatException;
import com.gamegoo.gamegoo_v2.core.exception.common.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ChatCommandService {

    private final MemberValidator memberValidator;
    private final BlockValidator blockValidator;
    private final MemberRepository memberRepository;
    private final ChatRepository chatRepository;
    private final MemberChatroomRepository memberChatroomRepository;
    private final ChatroomRepository chatroomRepository;
    private final BoardRepository boardRepository;
    private final ApplicationEventPublisher eventPublisher;


    /**
     * member를 해당 chatroom에 입장 처리하는 메소드
     *
     * @param member
     * @param targetMember
     * @param chatroom
     * @return
     */
    public MemberChatroom enterExistingChatroom(Member member, Member targetMember, Chatroom chatroom) {
        MemberChatroom memberChatroom = memberChatroomRepository
                .findByMemberIdAndChatroomId(member.getId(), chatroom.getId())
                .orElseThrow(() -> new ChatException(ErrorCode.CHATROOM_ACCESS_DENIED));

        // 내가 해당 채팅방을 퇴장한 상태인 경우
        if (memberChatroom.exited()) {
            // 상대방이 나를 차단한 경우
            blockValidator.throwIfBlocked(targetMember, member, ChatException.class,
                    ErrorCode.CHAT_START_FAILED_BLOCKED_BY_TARGET);

            // 상대방이 탈퇴한 경우
            memberValidator.throwIfBlind(targetMember, ChatException.class,
                    ErrorCode.CHAT_START_FAILED_TARGET_DEACTIVATED);
        }

        // lastViewDate 업데이트
        memberChatroom.updateLastViewDate(LocalDateTime.now());

        return memberChatroom;
    }

    /**
     * member와 targetMember 사이 새로운 chatroom 생성 및 저장하는 메소드
     *
     * @param member
     * @param targetMember
     * @return
     */
    public Chatroom createChatroom(Member member, Member targetMember) {
        Chatroom chatroom = Chatroom.create(UUID.randomUUID().toString());

        chatroomRepository.save(chatroom);

        createAndSaveMemberChatroom(member, chatroom, null);
        createAndSaveMemberChatroom(targetMember, chatroom, null);

        return chatroom;
    }

    /**
     * 회원 메시지 생성 및 저장 메소드
     *
     * @param member
     * @param chatroom
     * @param content
     * @return
     */
    public Chat createMemberChat(Member member, Chatroom chatroom, String content) {
        return chatRepository.save(Chat.builder()
                .chatroom(chatroom)
                .fromMember(member)
                .contents(content)
                .build());
    }

    /**
     * 시스템 메시지 생성 및 저장 메소드
     *
     * @param request
     * @param member
     * @param targetMember
     * @param chatroom
     */
    public List<Chat> createSystemChat(SystemFlagRequest request, Member member, Member targetMember,
                                       Chatroom chatroom) {
        Board board = boardRepository.findById(request.getBoardId())
                .orElseThrow(() -> new ChatException(ErrorCode.ADD_BOARD_SYSTEM_CHAT_FAILED));

        // member에게 보낼 시스템 메시지 생성 및 저장
        SystemMessageType systemType = SystemMessageType.of(request.getFlag());
        String memberMessage = systemType.getMessage();
        Chat systemChatToMember = createAndSaveSystemChat(chatroom, member, memberMessage, board, systemType.getCode());

        // targetMember에게 보낼 시스템 메시지 생성 및 저장
        SystemMessageType targetSystemType = SystemMessageType.INCOMING_CHAT_BY_BOARD_MESSAGE;
        Chat systemChatToTargetMember = createAndSaveSystemChat(chatroom, targetMember, targetSystemType.getMessage(),
                board, targetSystemType.getCode());

        return List.of(systemChatToMember, systemChatToTargetMember);
    }

    /**
     * member의 lastViewDate 업데이트 메소드
     *
     * @param member
     * @param chatroom
     * @param lastViewDate
     * @return
     */
    public MemberChatroom updateLastViewDate(Member member, Chatroom chatroom, LocalDateTime lastViewDate) {
        MemberChatroom memberChatroom = memberChatroomRepository
                .findByMemberIdAndChatroomId(member.getId(), chatroom.getId())
                .orElseThrow(() -> new ChatException(ErrorCode.CHATROOM_ACCESS_DENIED));

        memberChatroom.updateLastViewDate(lastViewDate);

        return memberChatroom;
    }

    /**
     * member와 targetMember의 lastJoinDate 업데이트 및 socket join 이벤트 발생
     *
     * @param member
     * @param targetMember
     * @param memberCreatedAt
     * @param targetCreatedAt
     * @param chatroom
     */
    public void updateLastJoinDates(Member member, Member targetMember, LocalDateTime memberCreatedAt,
                                    LocalDateTime targetCreatedAt, Chatroom chatroom) {
        MemberChatroom memberChatroom = memberChatroomRepository
                .findByMemberIdAndChatroomId(member.getId(), chatroom.getId())
                .orElseThrow(() -> new ChatException(ErrorCode.CHATROOM_ACCESS_DENIED));

        MemberChatroom targetMemberChatroom = memberChatroomRepository
                .findByMemberIdAndChatroomId(targetMember.getId(), chatroom.getId())
                .orElseThrow(() -> new ChatException(ErrorCode.CHATROOM_ACCESS_DENIED));

        updateLastJoinDate(member, memberChatroom, memberCreatedAt);
        updateLastJoinDate(targetMember, targetMemberChatroom, targetCreatedAt);
    }

    /**
     * 새로운 채팅 등록 시 member의 lastViewDate, member와 targetMember의 lastJoinDate 업데이트
     *
     * @param member
     * @param targetMember
     * @param chat
     */
    public void updateMemberChatroomDatesByAddChat(Member member, Member targetMember, Chat chat) {
        // member의 lastViewDate 업데이트
        MemberChatroom memberChatroom = memberChatroomRepository
                .findByMemberIdAndChatroomId(member.getId(), chat.getChatroom().getId())
                .orElseThrow(() -> new ChatException(ErrorCode.CHATROOM_ACCESS_DENIED));

        memberChatroom.updateLastViewDate(chat.getCreatedAt());

        // member의 lastJoinDate 업데이트
        updateLastJoinDate(member, memberChatroom, chat.getCreatedAt());

        // targetMember의 lastJoinDate 업데이트
        MemberChatroom targetMemberChatroom = memberChatroomRepository
                .findByMemberIdAndChatroomId(targetMember.getId(), chat.getChatroom().getId())
                .orElseThrow(() -> new ChatException(ErrorCode.CHATROOM_ACCESS_DENIED));

        updateLastJoinDate(targetMember, targetMemberChatroom, chat.getCreatedAt());
    }

    /**
     * 해당 회원 및 채팅방에 대한 MemberChatroom 엔티티 생성 및 저장
     *
     * @param member
     * @param chatroom
     * @param lastJoinDate
     */
    private void createAndSaveMemberChatroom(Member member, Chatroom chatroom, LocalDateTime lastJoinDate) {
        memberChatroomRepository.save(MemberChatroom.create(member, chatroom, lastJoinDate));
    }

    /**
     * 시스템 메시지 생성 및 저장
     *
     * @param chatroom
     * @param toMember
     * @param content
     * @param sourceBoard
     * @return
     */
    private Chat createAndSaveSystemChat(Chatroom chatroom, Member toMember, String content, Board sourceBoard,
                                         int systemType) {
        Member systemMember = memberRepository.findById(0L)
                .orElseThrow(() -> new ChatException(ErrorCode.SYSTEM_MEMBER_NOT_FOUND));

        return chatRepository.save(Chat.builder()
                .contents(content)
                .chatroom(chatroom)
                .fromMember(systemMember)
                .toMember(toMember)
                .sourceBoard(sourceBoard)
                .systemType(systemType)
                .build());
    }

    /**
     * 기존 lastJoinDate가 null인 경우 업데이트 및 socket join 이벤트 발생
     *
     * @param member
     * @param memberChatroom
     * @param date
     */
    private void updateLastJoinDate(Member member, MemberChatroom memberChatroom, LocalDateTime date) {
        if (memberChatroom.getLastJoinDate() == null) {
            memberChatroom.updateLastJoinDate(date);

            // socket join API 요청
            eventPublisher.publishEvent(new SocketJoinEvent(member.getId(), memberChatroom.getChatroom().getUuid()));
        }
    }

}
