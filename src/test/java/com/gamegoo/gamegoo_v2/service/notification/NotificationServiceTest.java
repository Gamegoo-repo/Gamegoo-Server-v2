package com.gamegoo.gamegoo_v2.service.notification;

import com.gamegoo.gamegoo_v2.account.member.domain.LoginType;
import com.gamegoo.gamegoo_v2.account.member.domain.Member;
import com.gamegoo.gamegoo_v2.account.member.domain.Tier;
import com.gamegoo.gamegoo_v2.account.member.repository.MemberRepository;
import com.gamegoo.gamegoo_v2.notification.domain.Notification;
import com.gamegoo.gamegoo_v2.notification.domain.NotificationType;
import com.gamegoo.gamegoo_v2.notification.domain.NotificationTypeTitle;
import com.gamegoo.gamegoo_v2.notification.repository.NotificationRepository;
import com.gamegoo.gamegoo_v2.notification.repository.NotificationTypeRepository;
import com.gamegoo.gamegoo_v2.notification.service.NotificationService;
import com.gamegoo.gamegoo_v2.social.manner.domain.MannerKeyword;
import com.gamegoo.gamegoo_v2.social.manner.repository.MannerKeywordRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SpringBootTest
class NotificationServiceTest {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private NotificationTypeRepository notificationTypeRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private MannerKeywordRepository mannerKeywordRepository;

    private static final String TARGET_EMAIL = "target@naver.com";
    private static final String TARGET_GAMENAME = "target";

    private Member member;

    @BeforeEach
    void setUp() {
        member = createMember("test@gmail.com", "member");
        initNotificationType();
    }

    @AfterEach
    void tearDown() {
        notificationRepository.deleteAllInBatch();
        notificationTypeRepository.deleteAllInBatch();
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

        assertThat(member.getNotificationList()).hasSize(1);
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

        assertThat(sourceMember.getNotificationList()).hasSize(1);
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

        assertThat(targetMember.getNotificationList()).hasSize(1);
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

        assertThat(targetMember.getNotificationList()).hasSize(1);
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

        assertThat(member.getNotificationList()).hasSize(1);
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

        assertThat(member.getNotificationList()).hasSize(1);
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

        assertThat(member.getNotificationList()).hasSize(1);
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

        assertThat(member.getNotificationList()).hasSize(1);
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

    private void initNotificationType() {
        notificationTypeRepository.save(NotificationType.create(NotificationTypeTitle.FRIEND_REQUEST_SEND));
        notificationTypeRepository.save(NotificationType.create(NotificationTypeTitle.FRIEND_REQUEST_RECEIVED));
        notificationTypeRepository.save(NotificationType.create(NotificationTypeTitle.FRIEND_REQUEST_ACCEPTED));
        notificationTypeRepository.save(NotificationType.create(NotificationTypeTitle.FRIEND_REQUEST_REJECTED));
        notificationTypeRepository.save(NotificationType.create(NotificationTypeTitle.MANNER_LEVEL_UP));
        notificationTypeRepository.save(NotificationType.create(NotificationTypeTitle.MANNER_LEVEL_DOWN));
        notificationTypeRepository.save(NotificationType.create(NotificationTypeTitle.MANNER_KEYWORD_RATED));
    }

}
