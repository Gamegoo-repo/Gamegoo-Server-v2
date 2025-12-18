package com.gamegoo.gamegoo_v2.core.event.listener;

import com.gamegoo.gamegoo_v2.account.member.domain.Member;
import com.gamegoo.gamegoo_v2.account.member.service.MemberService;
import com.gamegoo.gamegoo_v2.core.event.MannerLevelDownEvent;
import com.gamegoo.gamegoo_v2.core.event.MannerLevelUpEvent;
import com.gamegoo.gamegoo_v2.core.event.MannerRatingInsertEvent;
import com.gamegoo.gamegoo_v2.notification.domain.NotificationTypeTitle;
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
public class MannerRatingEventListener {

    private final NotificationService notificationService;
    private final MemberService memberService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleMannerRatingInsertEvent(MannerRatingInsertEvent event) {
        try {
            Member member = memberService.findMemberById(event.getMemberId());

            // 매너 평가 등록 알림 생성
            notificationService.createMannerRatingNotification(event.getMannerKeywordIdList(), member);
        } catch (Exception e) {
            log.error("Failed to create manner rating notifications", e);
        }
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleMannerLevelUpEvent(MannerLevelUpEvent event) {
        try {
            Member member = memberService.findMemberById(event.getMemberId());

            // 매너 레벨 상승 알림 생성
            notificationService.createMannerLevelNotification(NotificationTypeTitle.MANNER_LEVEL_UP, member,
                    event.getMannerLevel());
        } catch (Exception e) {
            log.error("Failed to create manner level up notifications", e);
        }
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlerMannerLevelDownEvent(MannerLevelDownEvent event) {
        try {
            Member member = memberService.findMemberById(event.getMemberId());

            // 매너 레벨 하락 알림 생성
            notificationService.createMannerLevelNotification(NotificationTypeTitle.MANNER_LEVEL_DOWN, member,
                    event.getMannerLevel());
        } catch (Exception e) {
            log.error("Failed to create manner level down notifications", e);
        }
    }

}
