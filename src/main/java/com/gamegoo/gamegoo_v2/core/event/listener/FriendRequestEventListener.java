package com.gamegoo.gamegoo_v2.core.event.listener;

import com.gamegoo.gamegoo_v2.account.member.domain.Member;
import com.gamegoo.gamegoo_v2.account.member.service.MemberService;
import com.gamegoo.gamegoo_v2.core.event.AcceptFriendRequestEvent;
import com.gamegoo.gamegoo_v2.core.event.RejectFriendRequestEvent;
import com.gamegoo.gamegoo_v2.core.event.SendFriendRequestEvent;
import com.gamegoo.gamegoo_v2.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class FriendRequestEventListener {

    private final NotificationService notificationService;
    private final MemberService memberService;

    /**
     * 친구 요청 전송 event listener
     *
     * @param event event
     */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleSendFriendRequestEvent(SendFriendRequestEvent event) {
        try {
            Member member = memberService.findMemberById(event.getMemberId());
            Member sourceMember = memberService.findMemberById(event.getSourceMemberId());

            // member가 sourceMember에게 친구 요청 전송했음 알림 생성
            notificationService.createSendFriendRequestNotification(member, sourceMember);

            // sourceMember가 member로부터 친구 요청 받았음 알림 생성
            notificationService.createReceivedFriendRequestNotification(sourceMember, member);
        } catch (Exception e) {
            log.error("Failed to create friend request notifications", e);
        }
    }

    /**
     * 친구 요청 수락 event listener
     *
     * @param event event
     */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleAcceptFriendRequestEvent(AcceptFriendRequestEvent event) {
        try {
            Member member = memberService.findMemberById(event.getMemberId());
            Member targetMember = memberService.findMemberById(event.getTargetMemberId());

            // targetMember에게 member가 친구 요청 수락했음 알림 생성
            notificationService.createAcceptFriendRequestNotification(targetMember, member);
        } catch (Exception e) {
            log.error("Failed to create accept friend request notifications", e);
        }
    }

    /**
     * 친구 요청 거절 event listener
     *
     * @param event event
     */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleRejectFriendRequestEvent(RejectFriendRequestEvent event) {
        try {
            Member member = memberService.findMemberById(event.getMemberId());
            Member targetMember = memberService.findMemberById(event.getTargetMemberId());

            // targetMember에게 member가 친구 요청 거절했음 알림 생성
            notificationService.createRejectFriendRequestNotification(targetMember, member);
        } catch (Exception e) {
            log.error("Failed to create reject friend request notifications", e);
        }
    }

}
