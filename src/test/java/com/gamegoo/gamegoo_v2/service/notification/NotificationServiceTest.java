package com.gamegoo.gamegoo_v2.service.notification;

import com.gamegoo.gamegoo_v2.account.member.domain.LoginType;
import com.gamegoo.gamegoo_v2.account.member.domain.Member;
import com.gamegoo.gamegoo_v2.account.member.domain.Tier;
import com.gamegoo.gamegoo_v2.account.member.repository.MemberRepository;
import com.gamegoo.gamegoo_v2.core.exception.NotificationException;
import com.gamegoo.gamegoo_v2.notification.domain.Notification;
import com.gamegoo.gamegoo_v2.notification.domain.NotificationTypeTitle;
import com.gamegoo.gamegoo_v2.notification.repository.NotificationRepository;
import com.gamegoo.gamegoo_v2.notification.repository.NotificationTypeRepository;
import com.gamegoo.gamegoo_v2.notification.service.NotificationService;
import com.gamegoo.gamegoo_v2.social.manner.domain.MannerKeyword;
import com.gamegoo.gamegoo_v2.social.manner.repository.MannerKeywordRepository;
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
class NotificationServiceTest {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private MannerKeywordRepository mannerKeywordRepository;

    @Autowired
    private NotificationTypeRepository notificationTypeRepository;

    private static final String TARGET_EMAIL = "target@naver.com";
    private static final String TARGET_GAMENAME = "target";

    private Member member;

    @BeforeEach
    void setUp() {
        member = createMember("test@gmail.com", "member");
    }

    @AfterEach
    void tearDown() {
        notificationRepository.deleteAllInBatch();
        mannerKeywordRepository.deleteAllInBatch();
        memberRepository.deleteAllInBatch();
    }

    @DisplayName("친구 요청 전송됨 알림 생성 성공")
    @Test
    void createSendFriendRequestNotificationSucceeds() {
        // given
        Member sourceMember = createMember(TARGET_EMAIL, TARGET_GAMENAME);

        // when
        Notification notification = notificationService.createSendFriendRequestNotification(member, sourceMember);

        // then
        assertThat(notification.getNotificationType().getTitle()).isEqualTo(NotificationTypeTitle.FRIEND_REQUEST_SEND);
        assertThat(notification.isRead()).isFalse();
        assertThat(notification.getMember().getId()).isEqualTo(member.getId());
        assertThat(notification.getSourceMember().getId()).isEqualTo(sourceMember.getId());

        assertThat(notificationRepository.countByMemberId(member.getId())).isEqualTo(1);
    }

    @DisplayName("친구 요청 받음 알림 생성 성공")
    @Test
    void createReceivedFriendRequestNotificationSucceeds() {
        // given
        Member sourceMember = createMember(TARGET_EMAIL, TARGET_GAMENAME);

        // when
        Notification notification = notificationService.createReceivedFriendRequestNotification(sourceMember, member);

        // then
        assertThat(notification.getNotificationType().getTitle()).isEqualTo(
                NotificationTypeTitle.FRIEND_REQUEST_RECEIVED);
        assertThat(notification.isRead()).isFalse();
        assertThat(notification.getMember().getId()).isEqualTo(sourceMember.getId());
        assertThat(notification.getSourceMember().getId()).isEqualTo(member.getId());

        assertThat(notificationRepository.countByMemberId(sourceMember.getId())).isEqualTo(1);
    }

    @DisplayName("친구 요청 수락 알림 생성 성공")
    @Test
    void createAcceptFriendRequestNotificationSucceeds() {
        // given
        Member targetMember = createMember(TARGET_EMAIL, TARGET_GAMENAME);

        // when
        Notification notification = notificationService.createAcceptFriendRequestNotification(targetMember, member);

        // then
        assertThat(notification.getNotificationType().getTitle()).isEqualTo(
                NotificationTypeTitle.FRIEND_REQUEST_ACCEPTED);
        assertThat(notification.isRead()).isFalse();
        assertThat(notification.getMember().getId()).isEqualTo(targetMember.getId());
        assertThat(notification.getSourceMember().getId()).isEqualTo(member.getId());

        assertThat(notificationRepository.countByMemberId(targetMember.getId())).isEqualTo(1);
    }

    @DisplayName("친구 요청 거절 알림 생성 성공")
    @Test
    void createRejectFriendRequestNotificationSucceeds() {
        // given
        Member targetMember = createMember(TARGET_EMAIL, TARGET_GAMENAME);

        // when
        Notification notification = notificationService.createRejectFriendRequestNotification(targetMember, member);

        // then
        assertThat(notification.getNotificationType().getTitle()).isEqualTo(
                NotificationTypeTitle.FRIEND_REQUEST_REJECTED);
        assertThat(notification.isRead()).isFalse();
        assertThat(notification.getMember().getId()).isEqualTo(targetMember.getId());
        assertThat(notification.getSourceMember().getId()).isEqualTo(member.getId());

        assertThat(notificationRepository.countByMemberId(targetMember.getId())).isEqualTo(1);
    }

    @DisplayName("매너 레벨 상승 알림 생성 성공")
    @Test
    void createMannerLevelUpNotificationSucceeds() {
        // given
        int mannerLevel = 2;

        // when
        Notification notification = notificationService.createMannerLevelNotification(
                NotificationTypeTitle.MANNER_LEVEL_UP, member, mannerLevel);

        // then
        assertThat(notification.getNotificationType().getTitle()).isEqualTo(NotificationTypeTitle.MANNER_LEVEL_UP);
        assertThat(notification.isRead()).isFalse();
        assertThat(notification.getMember().getId()).isEqualTo(member.getId());
        assertThat(notification.getSourceMember()).isNull();
        assertThat(notification.getContent()).isEqualTo("매너레벨이 2단계로 올라갔어요!");

        assertThat(notificationRepository.countByMemberId(member.getId())).isEqualTo(1);
    }

    @DisplayName("매너 레벨 하락 알림 생성 성공")
    @Test
    void createMannerLevelDownNotificationSucceeds() {
        // given
        int mannerLevel = 1;

        // when
        Notification notification = notificationService.createMannerLevelNotification(
                NotificationTypeTitle.MANNER_LEVEL_DOWN, member, mannerLevel);

        // then
        assertThat(notification.getNotificationType().getTitle()).isEqualTo(NotificationTypeTitle.MANNER_LEVEL_DOWN);
        assertThat(notification.isRead()).isFalse();
        assertThat(notification.getMember().getId()).isEqualTo(member.getId());
        assertThat(notification.getSourceMember()).isNull();
        assertThat(notification.getContent()).isEqualTo("매너레벨이 1단계로 떨어졌어요.");

        assertThat(notificationRepository.countByMemberId(member.getId())).isEqualTo(1);
    }

    @DisplayName("매너 평가 등록 알림 생성 성공: 키워드가 여러개인 경우")
    @Test
    void createMannerRatingNotificationSucceedsWithManyKeywords() {
        // given
        List<MannerKeyword> mannerKeywordList = new ArrayList<>();
        mannerKeywordList.add(mannerKeywordRepository.save(MannerKeyword.create("캐리했어요", true)));
        mannerKeywordList.add(mannerKeywordRepository.save(MannerKeyword.create("1인분 이상은 해요", true)));
        mannerKeywordList.add(mannerKeywordRepository.save(MannerKeyword.create("욕 안해요", true)));

        List<Long> mannerKeywordIdList = mannerKeywordList.stream().map(MannerKeyword::getId).toList();

        // when
        Notification notification = notificationService.createMannerRatingNotification(mannerKeywordIdList, member);

        // then
        assertThat(notification.getNotificationType().getTitle()).isEqualTo(NotificationTypeTitle.MANNER_KEYWORD_RATED);
        assertThat(notification.isRead()).isFalse();
        assertThat(notification.getMember().getId()).isEqualTo(member.getId());
        assertThat(notification.getSourceMember()).isNull();
        assertThat(notification.getContent()).isEqualTo("지난 매칭에서 캐리했어요 외 2개의 키워드를 받았어요. 자세한 내용은 내 평가에서 확인하세요!");

        assertThat(notificationRepository.countByMemberId(member.getId())).isEqualTo(1);
    }

    @DisplayName("매너 평가 등록 알림 생성 성공: 키워드가 1개인 경우")
    @Test
    void createMannerRatingNotificationSucceedsWithSingleKeyword() {
        // given
        List<MannerKeyword> mannerKeywordList = new ArrayList<>();
        mannerKeywordList.add(mannerKeywordRepository.save(MannerKeyword.create("캐리했어요", true)));

        List<Long> mannerKeywordIdList = mannerKeywordList.stream().map(MannerKeyword::getId).toList();

        // when
        Notification notification = notificationService.createMannerRatingNotification(mannerKeywordIdList, member);

        // then
        assertThat(notification.getNotificationType().getTitle()).isEqualTo(NotificationTypeTitle.MANNER_KEYWORD_RATED);
        assertThat(notification.isRead()).isFalse();
        assertThat(notification.getMember().getId()).isEqualTo(member.getId());
        assertThat(notification.getSourceMember()).isNull();
        assertThat(notification.getContent()).isEqualTo("지난 매칭에서 캐리했어요 키워드를 받았어요. 자세한 내용은 내 평가에서 확인하세요!");

        assertThat(notificationRepository.countByMemberId(member.getId())).isEqualTo(1);
    }

    @DisplayName("신고 처리 결과 알림 생성 성공: 신고자용")
    @Test
    void createReportProcessedNotificationForReporterSucceeds() {
        // given
        String reportReason = "스팸 홍보/도배글";
        String banDescription = "1일 정지";

        // when
        Notification notification = notificationService.createReportProcessedNotificationForReporter(
                member, reportReason, banDescription);

        // then
        assertThat(notification.getNotificationType().getTitle()).isEqualTo(
                NotificationTypeTitle.REPORT_PROCESSED_REPORTER);
        assertThat(notification.isRead()).isFalse();
        assertThat(notification.getMember().getId()).isEqualTo(member.getId());
        assertThat(notification.getSourceMember()).isNull();
        assertThat(notification.getContent()).isEqualTo("신고 사유: 스팸 홍보/도배글\n처리 결과: 1일 정지");

        assertThat(notificationRepository.countByMemberId(member.getId())).isEqualTo(1);
    }

    @DisplayName("신고 처리 결과 알림 생성 성공: 신고당한 자용")
    @Test
    void createReportProcessedNotificationForReportedSucceeds() {
        // given
        String reportReason = "욕설/ 혐오/ 차별적 표현";
        String banDescription = "경고";

        // when
        Notification notification = notificationService.createReportProcessedNotificationForReported(
                member, reportReason, banDescription);

        // then
        assertThat(notification.getNotificationType().getTitle()).isEqualTo(
                NotificationTypeTitle.REPORT_PROCESSED_REPORTED);
        assertThat(notification.isRead()).isFalse();
        assertThat(notification.getMember().getId()).isEqualTo(member.getId());
        assertThat(notification.getSourceMember()).isNull();
        assertThat(notification.getContent()).isEqualTo("제한 사유: 욕설/ 혐오/ 차별적 표현\n제한 기간: 경고");

        assertThat(notificationRepository.countByMemberId(member.getId())).isEqualTo(1);
    }

    @Nested
    @DisplayName("신고 알림 에지 케이스")
    class ReportNotificationEdgeCaseTest {

        @DisplayName("실패: null member로 신고자 알림 생성")
        @Test
        void createReportProcessedNotificationForReporter_shouldThrowWhenMemberIsNull() {
            // given
            String reportReason = "스팸 홍보/도배글";
            String banDescription = "1일 정지";

            // when // then
            assertThatThrownBy(() -> notificationService.createReportProcessedNotificationForReporter(
                    null, reportReason, banDescription))
                    .isInstanceOf(NotificationException.class);
        }

        @DisplayName("실패: null member로 신고당한 자 알림 생성")
        @Test
        void createReportProcessedNotificationForReported_shouldThrowWhenMemberIsNull() {
            // given
            String reportReason = "욕설/ 혐오/ 차별적 표현";
            String banDescription = "경고";

            // when // then
            assertThatThrownBy(() -> notificationService.createReportProcessedNotificationForReported(
                    null, reportReason, banDescription))
                    .isInstanceOf(NotificationException.class);
        }

        @DisplayName("성공: 빈 문자열 파라미터로 알림 생성")
        @Test
        void createReportNotificationWithEmptyStrings() {
            // given
            String reportReason = "";
            String banDescription = "";

            // when
            Notification notification = notificationService.createReportProcessedNotificationForReporter(
                    member, reportReason, banDescription);

            // then
            assertThat(notification.getNotificationType().getTitle()).isEqualTo(
                    NotificationTypeTitle.REPORT_PROCESSED_REPORTER);
            assertThat(notification.getContent()).isEqualTo("신고 사유: \n처리 결과: ");
            assertThat(notificationRepository.countByMemberId(member.getId())).isEqualTo(1);
        }

        @DisplayName("성공: 긴 문자열 파라미터로 알림 생성")
        @Test
        void createReportNotificationWithLongStrings() {
            // given
            String reportReason = "매우 긴 신고 사유입니다. ".repeat(10); // 적절한 길이
            String banDescription = "매우 긴 제재 설명입니다. ".repeat(10);

            // when
            Notification notification = notificationService.createReportProcessedNotificationForReporter(
                    member, reportReason, banDescription);

            // then
            assertThat(notification.getNotificationType().getTitle()).isEqualTo(
                    NotificationTypeTitle.REPORT_PROCESSED_REPORTER);
            assertThat(notification.getContent()).contains(reportReason);
            assertThat(notification.getContent()).contains(banDescription);
            assertThat(notificationRepository.countByMemberId(member.getId())).isEqualTo(1);
        }

        @DisplayName("성공: 특수 문자가 포함된 파라미터로 알림 생성")
        @Test
        void createReportNotificationWithSpecialCharacters() {
            // given
            String reportReason = "신고 사유: <script>alert('xss')</script> & 특수문자 !@#$%^&*()";
            String banDescription = "제재: \"영구정지\" & 특수문자 []{}|\\;':\",./<>?";

            // when
            Notification notification = notificationService.createReportProcessedNotificationForReported(
                    member, reportReason, banDescription);

            // then
            assertThat(notification.getNotificationType().getTitle()).isEqualTo(
                    NotificationTypeTitle.REPORT_PROCESSED_REPORTED);
            assertThat(notification.getContent()).contains(reportReason);
            assertThat(notification.getContent()).contains(banDescription);
            assertThat(notificationRepository.countByMemberId(member.getId())).isEqualTo(1);
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
                .soloTier(Tier.IRON)
                .soloRank(0)
                .soloWinRate(0.0)
                .soloGameCount(0)
                .freeTier(Tier.IRON)
                .freeRank(0)
                .freeWinRate(0.0)
                .freeGameCount(0)
                .isAgree(true)
                .build());
    }

}
