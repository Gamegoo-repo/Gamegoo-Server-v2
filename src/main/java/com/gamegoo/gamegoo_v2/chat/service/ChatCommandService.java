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
     * 회원을 채팅방에 입장 처리하는 메소드
     *
     * @param member       회원
     * @param targetMember 상대 회원
     * @param chatroom     채팅방
     * @return MemberChatroom
     */
    public MemberChatroom enterExistingChatroom(Member member, Member targetMember, Chatroom chatroom) {
        MemberChatroom memberChatroom = memberChatroomRepository
                .findByMemberIdAndChatroomId(member.getId(), chatroom.getId())
                .orElseThrow(() -> new ChatException(ErrorCode.CHATROOM_ACCESS_DENIED));

        // 내가 해당 채팅방을 퇴장한 상태인 경우
        if (memberChatroom.exited()) {
            // 상대방이 탈퇴한 경우
            memberValidator.throwIfBlind(targetMember, ChatException.class,
                    ErrorCode.CHAT_START_FAILED_TARGET_DEACTIVATED);
        }

        // lastViewDate 업데이트
        memberChatroom.updateLastViewDate(LocalDateTime.now());

        return memberChatroom;
    }

    /**
     * 회원과 상대 회원 사이 새로운 채팅방 생성 및 저장하는 메소드
     *
     * @param member       회원
     * @param targetMember 상대 회원
     * @return Chatroom
     */
    public Chatroom createChatroom(Member member, Member targetMember) {
        Chatroom chatroom = Chatroom.create(UUID.randomUUID().toString());

        chatroomRepository.save(chatroom);

        createAndSaveMemberChatroom(member, chatroom, null);
        createAndSaveMemberChatroom(targetMember, chatroom, null);

        return chatroom;
    }

    /**
     * 회원 채팅 메시지 생성 및 저장 메소드
     *
     * @param member   회원
     * @param chatroom 채팅방
     * @param content  메시지 내용
     * @return Chat
     */
    public Chat createMemberChat(Member member, Chatroom chatroom, String content) {
        return chatRepository.save(Chat.create(content, null, chatroom, member, null, null));
    }

    /**
     * 시스템 메시지 생성 및 저장 메소드
     *
     * @param request      systemFlag 요청
     * @param member       회원
     * @param targetMember 상대 회원
     * @param chatroom     채팅방
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
     * 두 회원에게 매칭 성공 시스템 메시지 생성 및 저장 메소드
     *
     * @param member1  회원
     * @param member2  회원
     * @param chatroom 채팅방
     */
    public void createMatchingSystemChat(Member member1, Member member2, Chatroom chatroom) {
        SystemMessageType systemMessageType = SystemMessageType.MATCH_SUCCESS_MESSAGE;
        String message = systemMessageType.getMessage();

        createAndSaveSystemChat(chatroom, member1, message, null, systemMessageType.getCode());
        createAndSaveSystemChat(chatroom, member2, message, null, systemMessageType.getCode());
    }

    /**
     * 해당 회원에게 매너 평가 시스템 메시지 생성 및 저장 메소드
     *
     * @param member   회원
     * @param chatroom 채팅방
     * @return 저장된 시스템 메시지
     */
    public Chat createMannerSystemChat(Member member, Chatroom chatroom) {
        SystemMessageType systemMessageType = SystemMessageType.MANNER_MESSAGE;
        String message = systemMessageType.getMessage();

        return createAndSaveSystemChat(chatroom, member, message, null, systemMessageType.getCode());
    }

    /**
     * 회원의 lastViewDate 업데이트 메소드
     *
     * @param member       회원
     * @param chatroom     채팅방
     * @param lastViewDate lastViewDate
     * @return MemberChatroom
     */
    public MemberChatroom updateLastViewDate(Member member, Chatroom chatroom, LocalDateTime lastViewDate) {
        MemberChatroom memberChatroom = memberChatroomRepository
                .findByMemberIdAndChatroomId(member.getId(), chatroom.getId())
                .orElseThrow(() -> new ChatException(ErrorCode.CHATROOM_ACCESS_DENIED));

        memberChatroom.updateLastViewDate(lastViewDate);

        return memberChatroom;
    }

    /**
     * 회원과 상대 회원의 lastJoinDate 업데이트 및 socket join 이벤트 발생
     *
     * @param member         회원
     * @param targetMember   상대 회원
     * @param memberUpdateTo 회원의 lastJoinDate 업데이트 값
     * @param targetUpdateTo 상대 회원의 lastJoinDate 업데이트 값
     * @param chatroom       채팅방
     */
    public void updateLastJoinDates(Member member, Member targetMember, LocalDateTime memberUpdateTo,
                                    LocalDateTime targetUpdateTo, Chatroom chatroom) {
        updateLastJoinDate(member, chatroom.getId(), memberUpdateTo);
        updateLastJoinDate(targetMember, chatroom.getId(), targetUpdateTo);
    }

    /**
     * 새로운 채팅 등록 시 member의 lastViewDate, 회원과 상대 회원의 lastJoinDate 업데이트
     *
     * @param member       회원
     * @param targetMember 상대 회원
     * @param chat         채팅
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
        updateLastJoinDate(targetMember, chat.getChatroom().getId(), chat.getCreatedAt());
    }

    /**
     * 해당 회원 및 채팅방에 대한 MemberChatroom 엔티티 생성 및 저장
     *
     * @param member       회원
     * @param chatroom     채팅방
     * @param lastJoinDate lastJoinDate
     */
    private void createAndSaveMemberChatroom(Member member, Chatroom chatroom, LocalDateTime lastJoinDate) {
        memberChatroomRepository.save(MemberChatroom.create(member, chatroom, lastJoinDate));
    }

    /**
     * 시스템 메시지 생성 및 저장
     *
     * @param chatroom    채팅방
     * @param toMember    시스템 메시지 대상 회원
     * @param content     메시지 내용
     * @param sourceBoard 메시지 관련 게시글
     * @return Chat
     */
    private Chat createAndSaveSystemChat(Chatroom chatroom, Member toMember, String content, Board sourceBoard,
                                         int systemType) {
        Member systemMember = memberRepository.findById(0L)
                .orElseThrow(() -> new ChatException(ErrorCode.SYSTEM_MEMBER_NOT_FOUND));

        return chatRepository.save(Chat.create(content, systemType, chatroom, systemMember, toMember, sourceBoard));
    }

    /**
     * lastJoinDate 업데이트 메소드
     * 기존 lastJoinDate가 null인 경우 socket join 이벤트 발생
     *
     * @param member         회원
     * @param memberChatroom 회원-채팅방
     * @param date           업데이트할 lastJoinDate 값
     */
    public void updateLastJoinDate(Member member, MemberChatroom memberChatroom, LocalDateTime date) {
        if (memberChatroom.getLastJoinDate() == null && date != null) {
            memberChatroom.updateLastJoinDate(date);

            // socket join API 요청
            eventPublisher.publishEvent(new SocketJoinEvent(member.getId(), memberChatroom.getChatroom().getUuid()));
        }

        if (memberChatroom.getLastJoinDate() != null && date == null) {
            memberChatroom.updateLastJoinDate(date);
        }
    }

    /**
     * lastJoinDate 업데이트 메소드
     * 기존 lastJoinDate가 null인 경우 socket join 이벤트 발생
     *
     * @param member     회원
     * @param chatroomId 채팅방 id
     * @param date       업데이트할 lastJoinDate 값
     */
    public void updateLastJoinDate(Member member, Long chatroomId, LocalDateTime date) {
        MemberChatroom memberChatroom = memberChatroomRepository
                .findByMemberIdAndChatroomId(member.getId(), chatroomId)
                .orElseThrow(() -> new ChatException(ErrorCode.CHATROOM_ACCESS_DENIED));

        updateLastJoinDate(member, memberChatroom, date);
    }

    /**
     * 해당 회원의 모든 채팅방에서 퇴장 처리
     * 해당 회원의 모든 memberChatroom의 lastJoinDate를 null로 업데이트
     *
     * @param member 회원
     */
    public void exitAllChatroom(Member member) {
        memberChatroomRepository.updateLastJoinDateToNullByMember(member);
    }

    /**
     * chatroom 엔티티의 lastChatId와 lastChatAt 업데이트 메소드
     *
     * @param chat     채팅
     * @param chatroom 채팅방
     */
    public void updateLastChat(Chat chat, Chatroom chatroom) {
        chatroom.updateLastChatId(chat.getId());
        chatroom.updateLastChatAt(chat.getCreatedAt());
    }

}
