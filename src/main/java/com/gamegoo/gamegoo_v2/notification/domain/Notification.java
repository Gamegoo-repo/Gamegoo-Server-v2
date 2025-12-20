package com.gamegoo.gamegoo_v2.notification.domain;

import com.gamegoo.gamegoo_v2.account.member.domain.Member;
import com.gamegoo.gamegoo_v2.core.common.BaseDateTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notification extends BaseDateTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_id")
    private Long id;

    @Column(nullable = false, length = 500)
    private String content;

    @Column(nullable = false)
    private boolean isRead = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_member_id")
    private Member sourceMember;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notification_type_id", nullable = false)
    private NotificationType notificationType;

    public static Notification create(Member member, Member sourceMember, NotificationType notificationType,
                                      String content) {
        Notification notification = Notification.builder()
                .sourceMember(sourceMember)
                .notificationType(notificationType)
                .content(content)
                .build();
        notification.setMember(member); // 양방향 연관관계 설정
        return notification;
    }

    @Builder
    private Notification(String content, boolean isRead, Member sourceMember, Member member,
                         NotificationType notificationType) {
        this.content = content;
        this.isRead = isRead;
        this.sourceMember = sourceMember;
        this.member = member;
        this.notificationType = notificationType;
    }

    private void setMember(Member member) {
        if (this.member != null) {
            this.member.getNotificationList().remove(this);
        }
        this.member = member;
        //member.getNotificationList().add(this); // AFTER_COMMIT 이벤트에서 notification을 생성하므로 양방향 연관관계 설정x
    }

    public void updateIsRead(boolean isRead) {
        this.isRead = isRead;
    }

}
