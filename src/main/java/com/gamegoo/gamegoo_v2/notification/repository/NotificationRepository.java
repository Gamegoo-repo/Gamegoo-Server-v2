package com.gamegoo.gamegoo_v2.notification.repository;

import com.gamegoo.gamegoo_v2.account.member.domain.Member;
import com.gamegoo.gamegoo_v2.notification.domain.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long>, NotificationRepositoryCustom {

    boolean existsByMemberAndId(Member member, Long id);

    Page<Notification> findNotificationsByMember(Member member, Pageable pageable);

    int countByMemberIdAndIsReadFalse(Long memberId);

    int countByMemberId(Long memberId);

}
