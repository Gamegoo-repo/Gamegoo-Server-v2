package com.gamegoo.gamegoo_v2.notification.repository;

import com.gamegoo.gamegoo_v2.notification.domain.Notification;
import org.springframework.data.domain.Slice;

public interface NotificationRepositoryCustom {

    /**
     * 커서 기반 알림 목록 조회
     *
     * @param memberId 회원 id
     * @param cursor   알림 id
     * @param pageSize 페이지 크기
     * @return 알림 Slice 객체
     */
    Slice<Notification> findNotificationsByCursor(Long memberId, Long cursor, int pageSize);

}
