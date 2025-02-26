package com.gamegoo.gamegoo_v2.notification.dto;

import com.gamegoo.gamegoo_v2.notification.domain.Notification;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
@Builder
public class NotificationPageListResponse {

    List<NotificationResponse> notificationList;
    int listSize;
    int totalPage;
    long totalElements;
    Boolean isFirst;
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
