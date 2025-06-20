package com.gamegoo.gamegoo_v2.core.scheduler.handler;

import com.gamegoo.gamegoo_v2.account.member.domain.Member;
import com.gamegoo.gamegoo_v2.chat.domain.Chat;
import com.gamegoo.gamegoo_v2.chat.domain.SystemMessageType;
import com.gamegoo.gamegoo_v2.chat.service.ChatCommandService;
import com.gamegoo.gamegoo_v2.chat.service.ChatQueryService;
import com.gamegoo.gamegoo_v2.external.socket.SocketService;
import com.gamegoo.gamegoo_v2.matching.domain.MannerMessageStatus;
import com.gamegoo.gamegoo_v2.matching.domain.MatchingRecord;
import com.gamegoo.gamegoo_v2.matching.service.MatchingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class MannerMessageHandler {

    private final ChatQueryService chatQueryService;
    private final ChatCommandService chatCommandService;
    private final MatchingService matchingService;
    private final SocketService socketService;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void process(MatchingRecord matchingRecord) {
        Member targetMember = matchingService.getTargetMemberByMatchingUuid(matchingRecord.getMatchingUuid());

        chatQueryService.findExistingChatroom(matchingRecord.getMember(), targetMember)
                .ifPresentOrElse(chatroom -> {
                            // 매너평가 시스템 메시지 생성 및 저장
                            Chat createdChat = chatCommandService.createMannerSystemChat(matchingRecord.getMember(),
                                    chatroom);

                            // 매너평가 메시지 전송 여부 변경
                            matchingRecord.updateMannerMessageSent(MannerMessageStatus.SENT);

                            socketService.sendSystemMessage(matchingRecord.getMember().getId(), chatroom.getUuid(),
                                    SystemMessageType.MANNER_MESSAGE.getMessage(), createdChat.getTimestamp());
                        },
                        () -> log.info("Chatroom not found, member ID: {}, target member ID: {}",
                                matchingRecord.getMember().getId(), targetMember.getId())
                );
    }

}
