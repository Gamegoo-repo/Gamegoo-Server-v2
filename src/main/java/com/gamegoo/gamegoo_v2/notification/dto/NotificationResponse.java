package com.gamegoo.gamegoo_v2.notification.dto;

import com.gamegoo.gamegoo_v2.notification.domain.Notification;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class NotificationResponse {

    Long notificationId;
    int notificationType;
    String content;
    String pageUrl;
    boolean read;
    LocalDateTime createdAt;

    public static NotificationResponse of(Notification notification) {
        String pageUrl = createPageUrl(notification);
        String content = createContent(notification);

        return NotificationResponse.builder()
                .notificationId(notification.getId())
                .notificationType(notification.getNotificationType().getImgType())
                .content(content)
                .pageUrl(pageUrl)
                .read(notification.isRead())
                .createdAt(notification.getCreatedAt())
                .build();
    }

    private static String createPageUrl(Notification notification) {
        String sourceUrl = notification.getNotificationType().getSourceUrl();

        if (sourceUrl == null) {
            return null;
        }

        // sourceUrl을 기반으로 URL 생성
        StringBuilder urlBuilder = new StringBuilder(sourceUrl);

        // sourceMember가 없는 경우
        if (notification.getSourceMember() == null) {
            return urlBuilder.toString();
        }

        // sourceMember가 탈퇴한 회원인 경우 URL 생성하지 않음
        if (notification.getSourceMember().getBlind()) {
            return null;
        }

        // 탈퇴하지 않은 sourceMember의 id 추가
        return urlBuilder.append(notification.getSourceMember().getId()).toString();
    }

    private static String createContent(Notification notification) {
        String content = notification.getContent();

        // sourceMember가 없는 경우 content를 그대로 리턴
        if (notification.getSourceMember() == null) {
            return content;
        }

        // sourceMember 닉네임 표시
        if (notification.getSourceMember().getBlind()) { // sourceMember가 탈퇴한 회원인 경우
            return "(탈퇴한 사용자)" + content;
        } else {
            return notification.getSourceMember().getGameName() + content;
        }
    }

}
