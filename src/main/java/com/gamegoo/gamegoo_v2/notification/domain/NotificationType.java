package com.gamegoo.gamegoo_v2.notification.domain;

import com.gamegoo.gamegoo_v2.core.common.BaseDateTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NotificationType extends BaseDateTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_type_id")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "VARCHAR(30)")
    private NotificationTypeTitle title;

    @Column(nullable = false, length = 400)
    private String content;

    private String sourceUrl;

    @Column(nullable = false)
    private int imgType;

    public static NotificationType create(NotificationTypeTitle title) {
        return NotificationType.builder()
                .title(title)
                .content(title.getMessage())
                .sourceUrl(title.getSourceUrl())
                .imgType(title.getImgType())
                .build();
    }

    @Builder
    private NotificationType(NotificationTypeTitle title, String content, String sourceUrl, int imgType) {
        this.title = title;
        this.content = content;
        this.sourceUrl = sourceUrl;
        this.imgType = imgType;
    }

}
