package com.gamegoo.gamegoo_v2.notification.dto;

import com.gamegoo.gamegoo_v2.notification.domain.Notification;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
@Builder
public class NotificationPageListResponse {

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    List<NotificationResponse> notificationList;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    int listSize;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    int totalPage;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    long totalElements;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    Boolean isFirst;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    Boolean isLast;

    public static NotificationPageListResponse of(Page<Notification> notificationPage) {
        List<NotificationResponse> notificationList = notificationPage.stream()
                .map(NotificationResponse::of)
                .toList();

        return NotificationPageListResponse.builder()
                .notificationList(notificationList)
                .listSize(notificationList.size())
                .totalPage(notificationPage.getTotalPages())
                .totalElements(notificationPage.getTotalElements())
                .isFirst(notificationPage.isFirst())
                .isLast(notificationPage.isLast())
                .build();
    }


}
