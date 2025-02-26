package com.gamegoo.gamegoo_v2.chat.service;

import com.gamegoo.gamegoo_v2.account.member.domain.Member;
import com.gamegoo.gamegoo_v2.account.member.service.MemberService;
import com.gamegoo.gamegoo_v2.chat.domain.Chat;
import com.gamegoo.gamegoo_v2.chat.domain.Chatroom;
import com.gamegoo.gamegoo_v2.chat.domain.MemberChatroom;
import com.gamegoo.gamegoo_v2.chat.domain.SystemMessageType;
import com.gamegoo.gamegoo_v2.chat.dto.ChatResponseFactory;
import com.gamegoo.gamegoo_v2.chat.dto.request.ChatCreateRequest;
import com.gamegoo.gamegoo_v2.chat.dto.response.ChatCreateResponse;
import com.gamegoo.gamegoo_v2.chat.dto.response.ChatMessageListResponse;
import com.gamegoo.gamegoo_v2.chat.dto.response.ChatroomListResponse;
import com.gamegoo.gamegoo_v2.chat.dto.response.ChatroomResponse;
import com.gamegoo.gamegoo_v2.chat.dto.response.EnterChatroomResponse;
import com.gamegoo.gamegoo_v2.content.board.domain.Board;
import com.gamegoo.gamegoo_v2.content.board.service.BoardService;
import com.gamegoo.gamegoo_v2.core.common.validator.BlockValidator;
import com.gamegoo.gamegoo_v2.core.common.validator.ChatValidator;
import com.gamegoo.gamegoo_v2.core.common.validator.MemberValidator;
import com.gamegoo.gamegoo_v2.core.exception.ChatException;
import com.gamegoo.gamegoo_v2.core.exception.common.ErrorCode;
import com.gamegoo.gamegoo_v2.social.block.service.BlockService;
import com.gamegoo.gamegoo_v2.social.friend.service.FriendService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static com.gamegoo.gamegoo_v2.core.exception.common.ErrorCode.CHAT_ADD_FAILED_BLOCKED_BY_TARGET;
import static com.gamegoo.gamegoo_v2.core.exception.common.ErrorCode.CHAT_ADD_FAILED_TARGET_DEACTIVATED;
import static com.gamegoo.gamegoo_v2.core.exception.common.ErrorCode.CHAT_ADD_FAILED_TARGET_IS_BLOCKED;
import static com.gamegoo.gamegoo_v2.core.exception.common.ErrorCode.CHAT_START_FAILED_BLOCKED_BY_TARGET;
import static com.gamegoo.gamegoo_v2.core.exception.common.ErrorCode.CHAT_START_FAILED_TARGET_DEACTIVATED;
import static com.gamegoo.gamegoo_v2.core.exception.common.ErrorCode.CHAT_START_FAILED_TARGET_IS_BLOCKED;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatFacadeService {

    private final MemberService memberService;
    private final BoardService boardService;
    private final ChatCommandService chatCommandService;
    private final ChatQueryService chatQueryService;

    private final MemberValidator memberValidator;
    private final BlockValidator blockValidator;
    private final ChatValidator chatValidator;

    private final ChatResponseFactory chatResponseFactory;
    private final FriendService friendService;
    private final BlockService blockService;

    /**
     * 대상 회원과 채팅 시작 Facade 메소드
     * 기존에 채팅방이 있는 경우 채팅방 입장 처리, 기존에 채팅방이 없는 경우 새로운 채팅방 생성
     *
     * @param member         회원
     * @param targetMemberId 상대 회원 id
     * @return EnterChatroomResponse
     */
    @Transactional
    public EnterChatroomResponse startChatroomByMemberId(Member member, Long targetMemberId) {
        // 대상 회원 검증
        Member targetMember = memberService.findMemberById(targetMemberId);
        memberValidator.throwIfEqual(member, targetMember);

        // 내가 상대 회원을 차단하지 않았는지 검증
        blockValidator.throwIfBlocked(member, targetMember, ChatException.class, CHAT_START_FAILED_TARGET_IS_BLOCKED);

        Chatroom chatroom;

        Optional<Chatroom> existingChatroom = chatQueryService.findExistingChatroom(member, targetMember);

        if (existingChatroom.isPresent()) { // 기존 채팅방이 존재하는 경우
            chatroom = existingChatroom.get();

        } else {// 기존에 채팅방이 존재하지 않는 경우
            // 상대가 나를 차단하지 않았는지 검증
            blockValidator.throwIfBlocked(targetMember, member, ChatException.class,
                    CHAT_START_FAILED_BLOCKED_BY_TARGET);

            // 상대가 탈퇴하지 않았는지 검증
            memberValidator.throwIfBlind(targetMember, ChatException.class, CHAT_START_FAILED_TARGET_DEACTIVATED);

            // 새 채팅방 생성
            chatroom = chatCommandService.createChatroom(member, targetMember);
        }

        // 채팅방에 입장 처리
        chatCommandService.enterExistingChatroom(member, targetMember, chatroom);

        // 최근 메시지 내역 조회
        Slice<Chat> chatSlice = chatQueryService.getRecentChatSlice(member, chatroom);

        // 응답 dto 생성
        ChatMessageListResponse chatMessageListResponse = chatResponseFactory.toChatMessageListResponse(chatSlice);

        return chatResponseFactory.toEnterChatroomResponse(member, targetMember, chatroom.getUuid(),
                chatMessageListResponse);
    }

    /**
     * 게시글을 통한 채팅 시작 Facade 메소드
     * 기존에 채팅방이 있는 경우 채팅방 입장 처리, 기존에 채팅방이 없는 경우 새로운 채팅방 생성
     *
     * @param member  회원
     * @param boardId 게시글 id
     * @return EnterChatroomResponse
     */
    @Transactional
    public EnterChatroomResponse startChatroomByBoardId(Member member, Long boardId) {
        // 게시글 검증 및 조회
        Board board = boardService.findBoard(boardId);

        // 대상 회원 검증 및 조회
        Member targetMember = memberService.findMemberById(board.getMember().getId());
        memberValidator.throwIfEqual(member, targetMember);

        // 상대가 탈퇴하지 않았는지 검증
        memberValidator.throwIfBlind(targetMember, ChatException.class, CHAT_START_FAILED_TARGET_DEACTIVATED);

        // 상대가 나를 차단하지 않았는지 검증
        blockValidator.throwIfBlocked(targetMember, member, ChatException.class, CHAT_START_FAILED_BLOCKED_BY_TARGET);

        // 내가 상대 회원을 차단하지 않았는지 검증
        blockValidator.throwIfBlocked(member, targetMember, ChatException.class, CHAT_START_FAILED_TARGET_IS_BLOCKED);

        Chatroom chatroom = chatQueryService.findExistingChatroom(member, targetMember)
                .orElseGet(() -> chatCommandService.createChatroom(member, targetMember));

        // 채팅방에 입장 처리
        MemberChatroom memberChatroom = chatCommandService.enterExistingChatroom(member, targetMember, chatroom);

        // 최근 메시지 내역 조회
        Slice<Chat> chatSlice = chatQueryService.getRecentChatSlice(member, chatroom);

        // 응답 dto 생성
        ChatMessageListResponse chatMessageListResponse = chatResponseFactory.toChatMessageListResponse(chatSlice);
        int systemFlag = getSystemFlag(memberChatroom);
        return chatResponseFactory.toEnterChatroomResponse(member, targetMember, chatroom.getUuid(), systemFlag,
                boardId, chatMessageListResponse);
    }

    /**
     * uuid에 해당하는 채팅방에 입장 처리 Facade 메소드
     *
     * @param member 회원
     * @param uuid   채팅방 uuid
     * @return EnterChatroomResponse
     */
    @Transactional
    public EnterChatroomResponse enterChatroomByUuid(Member member, String uuid) {
        // chatroom 엔티티 조회
        Chatroom chatroom = chatQueryService.getChatroomByUuid(uuid);

        // 해당 채팅방이 회원의 것이 맞는지 검증
        chatValidator.validateMemberChatroom(member.getId(), chatroom.getId());

        // 내가 상대 회원을 차단하지 않았는지 검증
        Member targetMember = chatQueryService.getChatroomTargetMember(member, chatroom);
        blockValidator.throwIfBlocked(member, targetMember, ChatException.class, CHAT_START_FAILED_TARGET_IS_BLOCKED);

        // 채팅방에 입장 처리
        chatCommandService.enterExistingChatroom(member, targetMember, chatroom);

        // 최근 메시지 내역 조회
        Slice<Chat> chatSlice = chatQueryService.getRecentChatSlice(member, chatroom);

        // 응답 dto 생성
        ChatMessageListResponse chatMessageListResponse = chatResponseFactory.toChatMessageListResponse(chatSlice);

        return chatResponseFactory.toEnterChatroomResponse(member, targetMember, chatroom.getUuid(),
                chatMessageListResponse);
    }

    /**
     * uuid에 해당하는 채팅방에 새로운 채팅 등록 Facade 메소드
     *
     * @param request  채팅 등록 요청
     * @param memberId 회원 id
     * @param uuid     채팅방 uuid
     * @return ChatCreateResponse
     */
    @Transactional
    public ChatCreateResponse createChat(ChatCreateRequest request, Long memberId, String uuid) {
        // member 엔티티 조회
        Member member = memberService.findMemberById(memberId);

        // chatroom 엔티티 조회
        Chatroom chatroom = chatQueryService.getChatroomByUuid(uuid);

        // 해당 채팅방이 회원의 것이 맞는지 검증
        chatValidator.validateMemberChatroom(member.getId(), chatroom.getId());

        // 상대가 탈퇴하지 않았는지 검증
        Member targetMember = chatQueryService.getChatroomTargetMember(member, chatroom);
        memberValidator.throwIfBlind(targetMember, ChatException.class, CHAT_ADD_FAILED_TARGET_DEACTIVATED);

        // 서로를 차단하지 않았는지 검증
        blockValidator.throwIfBlocked(member, targetMember, ChatException.class, CHAT_ADD_FAILED_TARGET_IS_BLOCKED);
        blockValidator.throwIfBlocked(targetMember, member, ChatException.class, CHAT_ADD_FAILED_BLOCKED_BY_TARGET);

        Chat chat;
        // 등록해야 할 시스템 메시지가 있는 경우
        if (request.getSystem() != null) {
            // 시스템 메시지 생성
            List<Chat> chats = chatCommandService.createSystemChat(request.getSystem(), member, targetMember,
                    chatroom);

            // member와 targetMember의 lastJoinDate 업데이트
            Chat systemChatToMember = chats.get(0);
            Chat systemChatToTargetMember = chats.get(1);
            chatCommandService.updateLastJoinDates(member, targetMember, systemChatToMember.getCreatedAt(),
                    systemChatToTargetMember.getCreatedAt(), chatroom);

            // 채팅 생성 및 저장
            chat = chatCommandService.createMemberChat(member, chatroom, request.getMessage());

            // member의 lastViewDate 업데이트
            chatCommandService.updateLastViewDate(member, chatroom, chat.getCreatedAt());
        } else {
            // 채팅 생성 및 저장
            chat = chatCommandService.createMemberChat(member, chatroom, request.getMessage());

            // member와 targetMember의 lastViewDate 및 lastJoinDate 업데이트
            chatCommandService.updateMemberChatroomDatesByAddChat(member, targetMember, chat);
        }

        chatCommandService.updateLastChat(chat, chatroom);

        return ChatCreateResponse.of(chat);
    }

    /**
     * 해당 채팅방의 대화 내역을 cursor 기반 페이징 조회 Facade 메소드
     *
     * @param member 회원
     * @param uuid   채팅방 uuid
     * @param cursor 채팅 timestamp
     * @return ChatMessageListResponse
     */
    public ChatMessageListResponse getChatMessagesByCursor(Member member, String uuid, Long cursor) {
        // chatroom 엔티티 조회
        Chatroom chatroom = chatQueryService.getChatroomByUuid(uuid);

        // 해당 채팅방이 회원의 것이 맞는지 검증
        chatValidator.validateMemberChatroom(member.getId(), chatroom.getId());

        Slice<Chat> chatSlice;
        if (cursor == null) { // cursor가 null인 경우
            // 최근 대화 내역 조회
            chatSlice = chatQueryService.getRecentChatSlice(member, chatroom);
        } else {
            // 커서 기반 대화 내역 조회
            chatSlice = chatQueryService.getChatSliceByCursor(member, chatroom, cursor);
        }

        return chatResponseFactory.toChatMessageListResponse(chatSlice);
    }

    /**
     * 해당 회원의 안읽은 메시지가 존재하는 채팅방 uuid 목록 조회 Facade 메소드
     *
     * @param member 회원
     * @return 채팅방 uuid list
     */
    public List<String> getUnreadChatroomUuids(Member member) {
        // 입장 상태인 모든 chatroom 조회
        List<Chatroom> activeChatrooms = chatQueryService.getActiveChatrooms(member);

        // 각 채팅방에 대해 안읽은 메시지 개수 조회
        return activeChatrooms.stream()
                .map(chatroom -> {
                    int unreadChats = chatQueryService.countUnreadChats(member, chatroom);

                    return unreadChats > 0 ? chatroom.getUuid() : null;
                })
                .filter(Objects::nonNull)
                .toList();
    }

    /**
     * 해당 채팅방 메시지 읽음 처리 Facade 메소드
     *
     * @param member    회원
     * @param uuid      채팅방 uuid
     * @param timestamp 채팅 timestamp
     * @return 처리 결과
     */
    @Transactional
    public String readChatMessage(Member member, String uuid, Long timestamp) {
        // chatroom 엔티티 조회
        Chatroom chatroom = chatQueryService.getChatroomByUuid(uuid);

        // 해당 채팅방이 회원의 것이 맞는지 검증
        MemberChatroom memberChatroom = chatValidator.validateMemberChatroom(member.getId(), chatroom.getId());

        // 채팅방에 입장한 상태가 맞는지 검증
        chatValidator.throwIfExited(memberChatroom, ChatException.class,
                ErrorCode.CHAT_READ_FAILED_NOT_ENTERED_CHATROOM);

        if (timestamp == null) {
            // timestamp가 없는 경우 현재 시각으로 lastViewDate 업데이트
            chatCommandService.updateLastViewDate(member, chatroom, LocalDateTime.now());
        } else {
            // timestamp가 있는 경우 해당 채팅의 createdAt으로 lastViewDate 업데이트
            Chat chat = chatQueryService.getChatByChatroomAndTimestamp(chatroom, timestamp);
            chatCommandService.updateLastViewDate(member, chatroom, chat.getCreatedAt());
        }

        return ("채팅 메시지 읽음 처리 성공");
    }

    /**
     * 해당 채팅방을 퇴장 처리 Facade 메소드
     *
     * @param member 회원
     * @param uuid   채팅방 uuid
     * @return 처리 결과
     */
    @Transactional
    public String exitChatroom(Member member, String uuid) {
        // chatroom 엔티티 조회
        Chatroom chatroom = chatQueryService.getChatroomByUuid(uuid);

        // 해당 채팅방이 회원의 것이 맞는지 검증
        MemberChatroom memberChatroom = chatValidator.validateMemberChatroom(member.getId(), chatroom.getId());

        // lastJoinDate를 null로 업데이트
        chatCommandService.updateLastJoinDate(member, memberChatroom, null);

        return "채팅방 나가기 성공";
    }

    /**
     * 해당 회원의 입장 상태인 전체 채팅방 목록 조회
     *
     * @param member 회원
     * @return ChatroomListResponse
     */
    public ChatroomListResponse getChatrooms(Member member) {
        // 입장 상태인 모든 memberChatroom 엔티티 정렬해 조회
        List<MemberChatroom> activeMemberChatrooms = chatQueryService.getActiveMemberChatrooms(member);

        if (activeMemberChatrooms.isEmpty()) {
            return chatResponseFactory.toChatroomListResponse();
        }

        List<Chatroom> chatrooms = activeMemberChatrooms.stream()
                .map(MemberChatroom::getChatroom)
                .toList();

        List<Long> chatroomIds = new ArrayList<>(chatrooms.size());
        List<Long> lastChatIds = new ArrayList<>(chatrooms.size());

        for (Chatroom cr : chatrooms) {
            chatroomIds.add(cr.getId());
            lastChatIds.add(cr.getLastChatId());
        }

        // 마지막 채팅 메시지 배치 조회
        Map<Long, Chat> lastChatMap = chatQueryService.findAllChatsBatch(lastChatIds);

        // 안읽은 메시지 수 배치 조회
        Map<Long, Integer> unreadChatsMap = chatQueryService.countUnreadChatsBatch(member, chatroomIds);

        // 채팅 상대 회원 배치 조회
        Map<Long, Member> targetMemberMap = chatQueryService.getChatroomTargetMembersBatch(member, chatroomIds);

        List<Member> targetMembers = new ArrayList<>(targetMemberMap.values());
        List<Long> targetMemberIds = targetMembers.stream()
                .map(Member::getId)
                .toList();

        // 상대 회원과 친구 여부, 차단 여부, 친구 요청 배치 조회
        Map<Long, Boolean> isFriendMap = friendService.isFriendBatch(member, targetMemberIds);
        Map<Long, Boolean> isBlockedMap = blockService.isBlockedByTargetMembersBatch(member, targetMemberIds);
        Map<Long, Long> friendRequestMap = friendService.getFriendRequestMemberIdBatch(member, targetMemberIds);

        // dto 생성
        List<ChatroomResponse> chatroomResponses = activeMemberChatrooms.stream()
                .map(mc -> {
                    Chatroom chatroom = mc.getChatroom();
                    Chat lastChat = lastChatMap.getOrDefault(chatroom.getId(), null);
                    Member targetMember = targetMemberMap.get(chatroom.getId());
                    Integer unreadCnt = unreadChatsMap.get(chatroom.getId());
                    boolean friend = isFriendMap.get(targetMember.getId());
                    boolean blocked = isBlockedMap.get(targetMember.getId());
                    Long friendRequestMemberId = friendRequestMap.get(targetMember.getId());

                    return chatResponseFactory.toChatroomResponse(chatroom, targetMember, friend, blocked,
                            friendRequestMemberId, lastChat, unreadCnt);
                }).toList();

        return chatResponseFactory.toChatroomListResponse(chatroomResponses);
    }

    /**
     * 해당 회원의 모든 채팅방 uuid 조회 Facade 메소드
     *
     * @param memberId 회원 id
     * @return 채팅방 uuid list
     */
    public List<String> getChatroomUuids(Long memberId) {
        Member member = memberService.findMemberById(memberId);
        List<Chatroom> chatrooms = chatQueryService.getActiveChatrooms(member);

        return chatrooms.stream()
                .map(Chatroom::getUuid)
                .toList();
    }

    /**
     * systemFlag 값 반환 메소드
     *
     * @param memberChatroom 회원-채팅방
     * @return systemFlag
     */
    private int getSystemFlag(MemberChatroom memberChatroom) {
        if (memberChatroom.exited()) {
            return SystemMessageType.INITIATE_CHATROOM_BY_BOARD_MESSAGE.getCode();
        }

        return SystemMessageType.CHAT_STARTED_BY_BOARD_MESSAGE.getCode();
    }

}
