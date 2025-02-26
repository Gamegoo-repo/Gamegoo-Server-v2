package com.gamegoo.gamegoo_v2.notification.dto;

import com.gamegoo.gamegoo_v2.notification.domain.Notification;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Slice;

import java.util.List;

@Getter
@Builder
public class NotificationCursorListResponse {

    List<NotificationResponse> notificationList;
    int listSize;
    boolean hasNext;
    Long nextCursor;

    public static NotificationCursorListResponse of(Slice<Notification> notificationSlice) {
        List<NotificationResponse> notificationResponseList = notificationSlice.stream()
                .map(NotificationResponse::of)
                .toList();

        Long nextCursor = notificationSlice.hasNext()
                ? notificationSlice.getContent().get(notificationResponseList.size() - 1).getId()
                : null;

        return NotificationCursorListResponse.builder()
                .notificationList(notificationResponseList)
                .listSize(notificationResponseList.size())
                .hasNext(notificationSlice.hasNext())
                .nextCursor(nextCursor)
                .build();
    }

}
