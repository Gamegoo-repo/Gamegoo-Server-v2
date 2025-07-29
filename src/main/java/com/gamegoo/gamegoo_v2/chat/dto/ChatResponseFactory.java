package com.gamegoo.gamegoo_v2.chat.dto;

import com.gamegoo.gamegoo_v2.account.member.domain.Member;
import com.gamegoo.gamegoo_v2.chat.domain.Chat;
import com.gamegoo.gamegoo_v2.chat.dto.data.ChatroomSummaryDTO;
import com.gamegoo.gamegoo_v2.chat.dto.response.ChatMessageListResponse;
import com.gamegoo.gamegoo_v2.chat.dto.response.ChatMessageResponse;
import com.gamegoo.gamegoo_v2.chat.dto.response.ChatroomListResponse;
import com.gamegoo.gamegoo_v2.chat.dto.response.ChatroomResponse;
import com.gamegoo.gamegoo_v2.chat.dto.response.EnterChatroomResponse;
import com.gamegoo.gamegoo_v2.chat.dto.response.SystemMessageResponse;
import com.gamegoo.gamegoo_v2.social.block.service.BlockService;
import com.gamegoo.gamegoo_v2.social.friend.service.FriendService;
import com.gamegoo.gamegoo_v2.utils.DateTimeUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ChatResponseFactory {

    private final FriendService friendService;
    private final BlockService blockService;

    public ChatMessageListResponse toChatMessageListResponse(Slice<Chat> chatSlice) {
        List<ChatMessageResponse> chatMessageResponseList = chatSlice.stream()
                .map(chat -> {
                    if (chat.getSystemType() == null) {
                        return ChatMessageResponse.of(chat);
                    }
                    return SystemMessageResponse.of(chat);
                })
                .toList();

        Long nextCursor = chatSlice.hasNext()
                ? chatSlice.getContent().get(0).getTimestamp()
                : null;

        return ChatMessageListResponse.builder()
                .chatMessageList(chatMessageResponseList)
                .listSize(chatMessageResponseList.size())
                .hasNext(chatSlice.hasNext())
                .nextCursor(nextCursor)
                .build();
    }

    public ChatMessageListResponse toChatMessageListResponse() {
        return ChatMessageListResponse.builder()
                .chatMessageList(new ArrayList<>())
                .listSize(0)
                .hasNext(false)
                .nextCursor(null)
                .build();
    }

    public EnterChatroomResponse toEnterChatroomResponse(Member member, Member targetMember, String chatroomUuid,
                                                         Integer systemFlag, Long boardId,
                                                         ChatMessageListResponse chatMessageListResponse) {
        String gameName = targetMember.getBlind()
                ? "(탈퇴한 사용자)"
                : targetMember.getGameName();

        EnterChatroomResponse.SystemFlagResponse systemFlagResponse =
                EnterChatroomResponse.SystemFlagResponse.of(systemFlag, boardId);

        return EnterChatroomResponse.builder()
                .uuid(chatroomUuid)
                .memberId(targetMember.getId())
                .gameName(gameName)
                .memberProfileImg(targetMember.getProfileImage())
                .friend(friendService.isFriend(member, targetMember))
                .blocked(blockService.isBlocked(member, targetMember))
                .blind(targetMember.getBlind())
                .friendRequestMemberId(friendService.getFriendRequestMemberId(member, targetMember))
                .system(systemFlagResponse)
                .chatMessageListResponse(chatMessageListResponse)
                .build();
    }

    public EnterChatroomResponse toEnterChatroomResponse(Member member, Member targetMember, String chatroomUuid,
                                                         ChatMessageListResponse chatMessageListResponse) {
        String gameName = targetMember.getBlind()
                ? "(탈퇴한 사용자)"
                : targetMember.getGameName();

        return EnterChatroomResponse.builder()
                .uuid(chatroomUuid)
                .memberId(targetMember.getId())
                .gameName(gameName)
                .memberProfileImg(targetMember.getProfileImage())
                .friend(friendService.isFriend(member, targetMember))
                .blocked(blockService.isBlocked(member, targetMember))
                .blind(targetMember.getBlind())
                .friendRequestMemberId(friendService.getFriendRequestMemberId(member, targetMember))
                .system(null)
                .chatMessageListResponse(chatMessageListResponse)
                .build();
    }

    public ChatroomListResponse toChatroomListResponse() {
        return ChatroomListResponse.builder()
                .chatroomResponseList(new ArrayList<>())
                .listSize(0)
                .build();
    }

    public ChatroomListResponse toChatroomListResponse(List<ChatroomResponse> chatroomResponseList) {
        return ChatroomListResponse.builder()
                .chatroomResponseList(chatroomResponseList)
                .listSize(chatroomResponseList.size())
                .build();
    }

    public ChatroomResponse toChatroomResponse(Member targetMember, boolean isFriend, boolean isBlocked,
                                               Long friendRequestMemberId,
                                               ChatroomSummaryDTO chatroomSummaryDTO) {
        String gameName = targetMember.getBlind()
                ? "(탈퇴한 사용자)"
                : targetMember.getGameName();

        String lastMsgAt = null;

        if (chatroomSummaryDTO.getLastChatAt() != null) {
            lastMsgAt = DateTimeUtil.toKSTString(chatroomSummaryDTO.getLastChatAt());
        }

        return ChatroomResponse.builder()
                .chatroomId(chatroomSummaryDTO.getChatroomId())
                .uuid(chatroomSummaryDTO.getChatroomUuid())
                .targetMemberId(targetMember.getId())
                .targetMemberImg(targetMember.getProfileImage())
                .targetMemberName(gameName)
                .friend(isFriend)
                .blocked(isBlocked)
                .blind(targetMember.getBlind())
                .friendRequestMemberId(friendRequestMemberId)
                .lastMsg(chatroomSummaryDTO.getLastChat())
                .lastMsgAt(lastMsgAt)
                .notReadMsgCnt(chatroomSummaryDTO.getUnreadCnt())
                .lastMsgTimestamp(chatroomSummaryDTO.getLastChatTimestamp())
                .build();
    }


    public ChatroomResponse toChatroomResponse(boolean isFriend, boolean isBlocked,
                                               Long friendRequestMemberId,
                                               ChatroomSummaryDTO chatroomSummaryDTO) {
        String gameName = chatroomSummaryDTO.getBlind()
                ? "(탈퇴한 사용자)"
                : chatroomSummaryDTO.getTargetMemberName();

        String lastMsgAt = null;

        if (chatroomSummaryDTO.getLastChatAt() != null) {
            lastMsgAt = DateTimeUtil.toKSTString(chatroomSummaryDTO.getLastChatAt());
        }

        return ChatroomResponse.builder()
                .chatroomId(chatroomSummaryDTO.getChatroomId())
                .uuid(chatroomSummaryDTO.getChatroomUuid())
                .targetMemberId(chatroomSummaryDTO.getTargetMemberId())
                .targetMemberImg(chatroomSummaryDTO.getTargetMemberImg())
                .targetMemberName(gameName)
                .friend(isFriend)
                .blocked(isBlocked)
                .blind(chatroomSummaryDTO.getBlind())
                .friendRequestMemberId(friendRequestMemberId)
                .lastMsg(chatroomSummaryDTO.getLastChat())
                .lastMsgAt(lastMsgAt)
                .notReadMsgCnt(chatroomSummaryDTO.getUnreadCnt())
                .lastMsgTimestamp(chatroomSummaryDTO.getLastChatTimestamp())
                .build();
    }

}
