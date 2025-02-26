package com.gamegoo.gamegoo_v2.repository.notification;

import com.gamegoo.gamegoo_v2.account.member.domain.Member;
import com.gamegoo.gamegoo_v2.notification.domain.Notification;
import com.gamegoo.gamegoo_v2.notification.domain.NotificationType;
import com.gamegoo.gamegoo_v2.notification.domain.NotificationTypeTitle;
import com.gamegoo.gamegoo_v2.notification.repository.NotificationRepository;
import com.gamegoo.gamegoo_v2.repository.RepositoryTestSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class NotificationRepositoryTest extends RepositoryTestSupport {

    @Autowired
    private NotificationRepository notificationRepository;

    private static final int PAGE_SIZE = 10;

    private NotificationType testNotificationType;

    @BeforeEach
    void setUp() {
        testNotificationType = em.persist(NotificationType.create(NotificationTypeTitle.TEST_ALARM));
    }

    @Nested
    @DisplayName("알림 전체 목록 조회")
    class FindNotificationsByMemberTest {

        @DisplayName("알림이 없는 경우 빈 page를 반환해야 한다.")
        @Test
        void findNotificationsByMemberNoResult() {
            // given
            PageRequest pageRequest = PageRequest.of(0, PAGE_SIZE, Sort.by(Sort.Direction.DESC, "createdAt"));

            // when
            Page<Notification> notificationPage = notificationRepository.findNotificationsByMember(member, pageRequest);

            // then
            assertThat(notificationPage).isEmpty();
            assertThat(notificationPage.getTotalElements()).isEqualTo(0);
            assertThat(notificationPage.getTotalPages()).isEqualTo(0);
            assertThat(notificationPage.isFirst()).isTrue();
            assertThat(notificationPage.isLast()).isTrue();
        }

        @DisplayName("알림이 10개 이하일 때 첫번째 페이지 요청")
        @Test
        void findNotificationByMemberFirstPage() {
            // given
            for (int i = 1; i <= 5; i++) {
                createTestNotification(member);
            }

            PageRequest pageRequest = PageRequest.of(0, PAGE_SIZE, Sort.by(Sort.Direction.DESC, "createdAt"));

            // when
            Page<Notification> notificationPage = notificationRepository.findNotificationsByMember(member, pageRequest);

            // then
            assertThat(notificationPage).isNotEmpty();
            assertThat(notificationPage.getContent()).hasSize(5);
            assertThat(notificationPage.getTotalElements()).isEqualTo(5);
            assertThat(notificationPage.getTotalPages()).isEqualTo(1);
            assertThat(notificationPage.isFirst()).isTrue();
            assertThat(notificationPage.isLast()).isTrue();
            assertThat(notificationPage.getContent())
                    .isSortedAccordingTo(Comparator.comparing(Notification::getId).reversed());
        }

        @DisplayName("알림이 10개 초과일 때 두번째 페이지 요청")
        @Test
        void findNotificationByMemberSecondPage() {
            // given
            for (int i = 1; i <= 15; i++) {
                createTestNotification(member);
            }

            PageRequest pageRequest = PageRequest.of(1, PAGE_SIZE, Sort.by(Sort.Direction.DESC, "createdAt"));

            // when
            Page<Notification> notificationPage = notificationRepository.findNotificationsByMember(member, pageRequest);

            // then
            assertThat(notificationPage).isNotEmpty();
            assertThat(notificationPage.getContent()).hasSize(5);
            assertThat(notificationPage.getTotalElements()).isEqualTo(15);
            assertThat(notificationPage.getTotalPages()).isEqualTo(2);
            assertThat(notificationPage.isFirst()).isFalse();
            assertThat(notificationPage.isLast()).isTrue();
            assertThat(notificationPage.getContent())
                    .isSortedAccordingTo(Comparator.comparing(Notification::getId).reversed());
        }

    }

    @Nested
    @DisplayName("알림 팝업 목록 조회")
    class FindNotificationsByCursorTest {

        @DisplayName("알림 팝업 목록 조회: 알림이 0개인 경우 빈 slice를 반환해야 한다.")
        @Test
        void findNotificationsByCursorNoResult() {
            // when
            Slice<Notification> notificationSlice = notificationRepository.findNotificationsByCursor(member.getId(),
                    null, PAGE_SIZE);

            // then
            assertThat(notificationSlice).isEmpty();
            assertThat(notificationSlice.hasNext()).isFalse();
        }

        @DisplayName("알림 팝업 목록 조회: 알림 개수가 page size 이하이고 cursor로 null을 입력한 경우 첫 페이지를 조회해야 한다.")
        @Test
        void findNotificationsByCursorFirstPage() {
            // given
            List<Notification> notifications = new ArrayList<>();
            for (int i = 1; i <= 10; i++) {
                notifications.add(createTestNotification(member));
            }
            Long oldestId = notifications.get(0).getId();
            Long newestId = notifications.get(9).getId();

            // when
            Slice<Notification> notificationSlice = notificationRepository.findNotificationsByCursor(member.getId(),
                    null, PAGE_SIZE);

            // then
            assertThat(notificationSlice).hasSize(10);
            assertThat(notificationSlice.hasNext()).isFalse();
            assertThat(notificationSlice.getContent().get(0).getId()).isEqualTo(newestId);
            assertThat(notificationSlice.getContent().get(9).getId()).isEqualTo(oldestId);
            assertThat(notificationSlice.getContent())
                    .isSortedAccordingTo(Comparator.comparing(Notification::getId).reversed());
        }

        @DisplayName("알림 팝업 목록 조회: 알림 개수가 page size 이상이고 cursor를 정상 입력한 경우 다음 페이지를 조회해야 한다.")
        @Test
        void findNotificationsByCursorNextPage() {
            // given
            List<Notification> notifications = new ArrayList<>();
            for (int i = 1; i <= 20; i++) {
                notifications.add(createTestNotification(member));
            }
            Long newestId = notifications.get(9).getId();
            Long oldestId = notifications.get(0).getId();
            Long cursorId = notifications.get(10).getId();

            // when
            Slice<Notification> notificationSlice = notificationRepository.findNotificationsByCursor(member.getId(),
                    cursorId, PAGE_SIZE);

            // then
            assertThat(notificationSlice).hasSize(10);
            assertThat(notificationSlice.hasNext()).isFalse();
            assertThat(notificationSlice.getContent().get(0).getId()).isEqualTo(newestId);
            assertThat(notificationSlice.getContent().get(9).getId()).isEqualTo(oldestId);
            assertThat(notificationSlice.getContent())
                    .isSortedAccordingTo(Comparator.comparing(Notification::getId).reversed());
        }

    }

    private Notification createTestNotification(Member member) {
        return em.persist(Notification.create(member, null, testNotificationType, testNotificationType.getContent()));
    }

}
