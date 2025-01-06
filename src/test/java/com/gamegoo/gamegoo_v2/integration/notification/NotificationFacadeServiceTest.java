package com.gamegoo.gamegoo_v2.integration.notification;

import com.gamegoo.gamegoo_v2.core.exception.NotificationException;
import com.gamegoo.gamegoo_v2.core.exception.common.ErrorCode;
import com.gamegoo.gamegoo_v2.account.member.domain.LoginType;
import com.gamegoo.gamegoo_v2.account.member.domain.Member;
import com.gamegoo.gamegoo_v2.account.member.domain.Tier;
import com.gamegoo.gamegoo_v2.account.member.repository.MemberRepository;
import com.gamegoo.gamegoo_v2.core.exception.NotificationException;
import com.gamegoo.gamegoo_v2.core.exception.common.ErrorCode;
import com.gamegoo.gamegoo_v2.notification.domain.Notification;
import com.gamegoo.gamegoo_v2.notification.domain.NotificationType;
import com.gamegoo.gamegoo_v2.notification.domain.NotificationTypeTitle;
import com.gamegoo.gamegoo_v2.notification.dto.NotificationCursorListResponse;
import com.gamegoo.gamegoo_v2.notification.dto.NotificationPageListResponse;
import com.gamegoo.gamegoo_v2.notification.dto.ReadNotificationResponse;
import com.gamegoo.gamegoo_v2.notification.repository.NotificationRepository;
import com.gamegoo.gamegoo_v2.notification.repository.NotificationTypeRepository;
import com.gamegoo.gamegoo_v2.notification.service.NotificationFacadeService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ActiveProfiles("test")
@SpringBootTest
class NotificationFacadeServiceTest {

    @Autowired
    private NotificationFacadeService notificationFacadeService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private NotificationTypeRepository notificationTypeRepository;

    private Member member;
    private NotificationType testNotificationType;

    @BeforeEach
    void setUp() {
        member = createMember("test@gmail.com", "member");
        testNotificationType = notificationTypeRepository.save(
                NotificationType.create(NotificationTypeTitle.TEST_ALARM));
    }

    @AfterEach
    void tearDown() {
        notificationRepository.deleteAllInBatch();
        notificationTypeRepository.deleteAllInBatch();
        memberRepository.deleteAllInBatch();
    }

    @Nested
    @DisplayName("알림 읽음 처리")
    class ReadNotificationTest {

        @DisplayName("알림 읽음 처리 성공")
        @Test
        void readNotificationSucceeds() {
            // given
            Notification notification = createTestNotification(member);

            // when
            ReadNotificationResponse response = notificationFacadeService.readNotification(member,
                    notification.getId());

            // then
            assertThat(response.getNotificationId()).isEqualTo(notification.getId());
            assertThat(response.getMessage()).isEqualTo("알림 읽음 처리 성공");

            // Notification 엔티티 상태가 정상 변경 되었는지 검증
            Notification updatedNotification = notificationRepository.findById(notification.getId()).get();
            assertThat(updatedNotification.isRead()).isTrue();
        }

        @DisplayName("알림 읽음 처리 실패: id에 해당하는 알림이 없는 경우 예외가 발생한다.")
        @Test
        void readNotification_shouldThrowWhenNotificationNotExists() {
            // when // then
            assertThatThrownBy(() -> notificationFacadeService.readNotification(member, 1L))
                    .isInstanceOf(NotificationException.class)
                    .hasMessage(ErrorCode.NOTIFICATION_NOT_FOUND.getMessage());
        }

    }

    @Nested
    @DisplayName("안읽은 알림 개수 조회")
    class CountUnreadNotificationTest {

        @DisplayName("안읽은 알림 개수 조회 성공")
        @Test
        void countUnreadNotificationSucceeds() {
            // given
            // 알림 생성
            Notification notification1 = createTestNotification(member);
            Notification notification2 = createTestNotification(member);
            createTestNotification(member);
            createTestNotification(member);
            createTestNotification(member);

            // 알림 2개 읽음 처리
            notification1.updateIsRead(true);
            notification2.updateIsRead(true);
            notificationRepository.save(notification1);
            notificationRepository.save(notification2);

            // when
            int count = notificationFacadeService.countUnreadNotification(member);

            // then
            assertThat(count).isEqualTo(3);
        }

    }

    @Nested
    @DisplayName("알림 전체 목록 조회")
    class GetNotificationPageListTest {

        @DisplayName("알림 전체 목록 조회 성공: 알림이 존재하지 않는 경우")
        @Test
        void getNotificationPageListSucceedsWhenNotificationNotExists() {
            // when
            NotificationPageListResponse response = notificationFacadeService.getNotificationPageList(member, 1);

            // then
            assertThat(response.getNotificationList()).isEmpty();
            assertThat(response.getListSize()).isEqualTo(0);
            assertThat(response.getTotalPage()).isEqualTo(0);
            assertThat(response.getTotalElements()).isEqualTo(0);
            assertThat(response.getIsFirst()).isTrue();
            assertThat(response.getIsLast()).isTrue();
        }

        @DisplayName("알림 전체 목록 조회 성공: 알림이 존재하는 경우")
        @Test
        void getNotificationPageListSucceedsWhenNotificationExists() {
            // given
            for (int i = 1; i <= 5; i++) {
                createTestNotification(member);
            }

            // when
            NotificationPageListResponse response = notificationFacadeService.getNotificationPageList(member, 1);

            // then
            assertThat(response.getNotificationList()).isNotEmpty();
            assertThat(response.getListSize()).isEqualTo(5);
            assertThat(response.getTotalPage()).isEqualTo(1);
            assertThat(response.getTotalElements()).isEqualTo(5);
            assertThat(response.getIsFirst()).isTrue();
            assertThat(response.getIsLast()).isTrue();
        }

    }

    @Nested
    @DisplayName("알림 팝업 목록 조회")
    class GetNotificationCursorListTest {

        @DisplayName("알림 팝업 목록 조회 성공: 알림이 존재하지 않는 경우")
        @Test
        void getNotificationCursorListSucceedsWhenNotificationNotExists() {
            // when
            NotificationCursorListResponse response = notificationFacadeService.getNotificationCursorList(member, 1L);

            // then
            assertThat(response.getNotificationList()).isEmpty();
            assertThat(response.getListSize()).isEqualTo(0);
            assertThat(response.getNextCursor()).isNull();
            assertThat(response.isHasNext()).isFalse();
        }

        @DisplayName("알림 팝업 목록 조회 성공: cursor를 입력하지 않은 경우")
        @Test
        void getNotificationCursorListSucceedsFirstPage() {
            // given
            for (int i = 1; i <= 5; i++) {
                createTestNotification(member);
            }

            // when
            NotificationCursorListResponse response = notificationFacadeService.getNotificationCursorList(member, null);

            // then
            assertThat(response.getNotificationList()).hasSize(5);
            assertThat(response.getListSize()).isEqualTo(5);
            assertThat(response.getNextCursor()).isNull();
            assertThat(response.isHasNext()).isFalse();
        }

        @DisplayName("알림 팝업 목록 조회 성공: 알림 개수가 page size 이상이고 cursor를 입력한 경우")
        @Test
        void getNotificationCursorListSucceedsNextPage() {
            // given
            List<Notification> notifications = new ArrayList<>();
            for (int i = 1; i <= 15; i++) {
                notifications.add(createTestNotification(member));
            }

            Long cursor = notifications.get(5).getId();

            // when
            NotificationCursorListResponse response = notificationFacadeService.getNotificationCursorList(member,
                    cursor);

            // then
            assertThat(response.getNotificationList()).hasSize(5);
            assertThat(response.getListSize()).isEqualTo(5);
            assertThat(response.getNextCursor()).isNull();
            assertThat(response.isHasNext()).isFalse();
        }

    }

    private Member createMember(String email, String gameName) {
        return memberRepository.save(Member.builder()
                .email(email)
                .password("testPassword")
                .profileImage(1)
                .loginType(LoginType.GENERAL)
                .gameName(gameName)
                .tag("TAG")
                .tier(Tier.IRON)
                .gameRank(0)
                .winRate(0.0)
                .gameCount(0)
                .isAgree(true)
                .build());
    }

    private Notification createTestNotification(Member member) {
        return notificationRepository.save(Notification.create(member, null, testNotificationType,
                testNotificationType.getContent()));
    }

}
