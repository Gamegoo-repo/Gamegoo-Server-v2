package com.gamegoo.gamegoo_v2.integration.manner;

import com.gamegoo.gamegoo_v2.account.member.domain.LoginType;
import com.gamegoo.gamegoo_v2.account.member.domain.Member;
import com.gamegoo.gamegoo_v2.account.member.domain.Tier;
import com.gamegoo.gamegoo_v2.account.member.repository.MemberRepository;
import com.gamegoo.gamegoo_v2.core.exception.MannerException;
import com.gamegoo.gamegoo_v2.core.exception.MemberException;
import com.gamegoo.gamegoo_v2.core.exception.common.ErrorCode;
import com.gamegoo.gamegoo_v2.core.exception.common.GlobalException;
import com.gamegoo.gamegoo_v2.notification.domain.NotificationTypeTitle;
import com.gamegoo.gamegoo_v2.notification.repository.NotificationRepository;
import com.gamegoo.gamegoo_v2.notification.service.NotificationService;
import com.gamegoo.gamegoo_v2.social.manner.domain.MannerRating;
import com.gamegoo.gamegoo_v2.social.manner.domain.MannerRatingKeyword;
import com.gamegoo.gamegoo_v2.social.manner.dto.request.MannerInsertRequest;
import com.gamegoo.gamegoo_v2.social.manner.dto.response.MannerInsertResponse;
import com.gamegoo.gamegoo_v2.social.manner.repository.MannerRatingKeywordRepository;
import com.gamegoo.gamegoo_v2.social.manner.repository.MannerRatingRepository;
import com.gamegoo.gamegoo_v2.social.manner.service.MannerFacadeService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ActiveProfiles("test")
@SpringBootTest
class MannerFacadeServiceTest {

    @Autowired
    private MannerFacadeService mannerFacadeService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private MannerRatingRepository mannerRatingRepository;

    @Autowired
    private MannerRatingKeywordRepository mannerRatingKeywordRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @MockitoSpyBean
    private NotificationService notificationService;

    private Member member;
    private Member targetMember;

    @BeforeEach
    void setUp() {
        member = createMember("test@gmail.com", "member");
        targetMember = createMember("target@gmail.com", "targetMember");
    }

    @AfterEach
    void tearDown() {
        notificationRepository.deleteAllInBatch();
        mannerRatingKeywordRepository.deleteAllInBatch();
        mannerRatingRepository.deleteAllInBatch();
        memberRepository.deleteAllInBatch();
    }

    @Nested
    @DisplayName("매너 평가 등록")
    class InsertPositiveMannerRatingTest {

        @DisplayName("실패: 대상 회원을 찾을 수 없는 경우 예외가 발생한다.")
        @Test
        void insertPositiveMannerRating_shouldThrownWhenMemberNotFound() {
            // given
            List<Long> mannerKeywordIds = List.of(1L, 2L, 3L);
            MannerInsertRequest request = MannerInsertRequest.builder()
                    .mannerKeywordIdList(mannerKeywordIds)
                    .build();

            // when // then
            assertThatThrownBy(() -> mannerFacadeService.insertPositiveMannerRating(member, 1000L, request))
                    .isInstanceOf(MemberException.class)
                    .hasMessage(ErrorCode.MEMBER_NOT_FOUND.getMessage());
        }

        @DisplayName("실패: 매너 키워드 id에 7 이상의 값이 들어간 경우 예외가 발생한다.")
        @Test
        void insertPositiveMannerRating_shouldThrownWhenMannerKeywordIsNotValid() {
            // given
            List<Long> mannerKeywordIds = List.of(1L, 2L, 7L);
            MannerInsertRequest request = MannerInsertRequest.builder()
                    .mannerKeywordIdList(mannerKeywordIds)
                    .build();

            // when // then
            assertThatThrownBy(
                    () -> mannerFacadeService.insertPositiveMannerRating(member, targetMember.getId(), request))
                    .isInstanceOf(MannerException.class)
                    .hasMessage(ErrorCode.MANNER_KEYWORD_INVALID.getMessage());
        }

        @DisplayName("실패: 매너 키워드 id에 음수 값이 들어간 경우 예외가 발생한다.")
        @Test
        void insertPositiveMannerRating_shouldThrownWhenMannerKeywordIsNegative() {
            // given
            List<Long> mannerKeywordIds = List.of(1L, 2L, -1L);
            MannerInsertRequest request = MannerInsertRequest.builder()
                    .mannerKeywordIdList(mannerKeywordIds)
                    .build();

            // when // then
            assertThatThrownBy(
                    () -> mannerFacadeService.insertPositiveMannerRating(member, targetMember.getId(), request))
                    .isInstanceOf(MannerException.class)
                    .hasMessage(ErrorCode.MANNER_KEYWORD_INVALID.getMessage());
        }

        @DisplayName("실패: 평가 대상으로 본인 id를 입력한 경우 예외가 발생한다.")
        @Test
        void insertPositiveMannerRating_shouldThrownWhenTargetIsSelf() {
            // given
            List<Long> mannerKeywordIds = List.of(1L, 2L, 3L);
            MannerInsertRequest request = MannerInsertRequest.builder()
                    .mannerKeywordIdList(mannerKeywordIds)
                    .build();

            // when // then
            assertThatThrownBy(
                    () -> mannerFacadeService.insertPositiveMannerRating(member, member.getId(), request))
                    .isInstanceOf(GlobalException.class)
                    .hasMessage(ErrorCode._BAD_REQUEST.getMessage());
        }

        @DisplayName("실패: 대상 회원이 탈퇴한 경우 예외가 발생한다.")
        @Test
        void insertPositiveMannerRating_shouldThrownWhenTargetIsBlind() {
            // given
            List<Long> mannerKeywordIds = List.of(1L, 2L, 3L);
            MannerInsertRequest request = MannerInsertRequest.builder()
                    .mannerKeywordIdList(mannerKeywordIds)
                    .build();

            // 대상 회원 탈퇴 처리
            targetMember.updateBlind(true);
            memberRepository.save(targetMember);

            // when // then
            assertThatThrownBy(
                    () -> mannerFacadeService.insertPositiveMannerRating(member, targetMember.getId(), request))
                    .isInstanceOf(MemberException.class)
                    .hasMessage(ErrorCode.TARGET_MEMBER_DEACTIVATED.getMessage());
        }

        @DisplayName("실패: 대상 회원에게 등록한 매너 평가가 존재하는 경우 예외가 발생한다.")
        @Test
        void insertPositiveMannerRating_shouldThrownWhenMannerRatingExists() {
            // given
            List<Long> mannerKeywordIds = List.of(1L, 2L, 3L);
            MannerInsertRequest request = MannerInsertRequest.builder()
                    .mannerKeywordIdList(mannerKeywordIds)
                    .build();

            mannerRatingRepository.save(MannerRating.create(member, targetMember, true));

            // when // then
            assertThatThrownBy(
                    () -> mannerFacadeService.insertPositiveMannerRating(member, targetMember.getId(), request))
                    .isInstanceOf(MannerException.class)
                    .hasMessage(ErrorCode.MANNER_RATING_EXISTS.getMessage());
        }

        @DisplayName("성공: 엔티티 생성 및 저장, 매너 평가 등록 알림이 생성되어야 한다.")
        @Test
        void insertPositiveMannerRatingSucceeds() {
            // given
            List<Long> mannerKeywordIds = List.of(1L, 2L, 3L);
            MannerInsertRequest request = MannerInsertRequest.builder()
                    .mannerKeywordIdList(mannerKeywordIds)
                    .build();

            // when
            MannerInsertResponse response = mannerFacadeService.insertPositiveMannerRating(member, targetMember.getId(),
                    request);

            // then
            assertThat(response.getTargetMemberId()).isEqualTo(targetMember.getId());
            assertThat(response.getMannerKeywordIdList()).isEqualTo(mannerKeywordIds);

            // mannerRating 엔티티 검증
            MannerRating mannerRating = mannerRatingRepository.findById(response.getMannerRatingId()).orElseThrow();
            assertThat(mannerRating.getFromMember().getId()).isEqualTo(member.getId());
            assertThat(mannerRating.getToMember().getId()).isEqualTo(targetMember.getId());
            assertThat(mannerRating.isPositive()).isTrue();

            // mannerRatingKeyword 엔티티 검증
            List<MannerRatingKeyword> mannerRatingKeywords = mannerRatingKeywordRepository.findByMannerRatingId(
                    mannerRating.getId());
            assertThat(mannerRatingKeywords).hasSize(mannerKeywordIds.size());

            // member의 매너 점수 업데이트 검증
            Member updatedMember = memberRepository.findById(targetMember.getId()).orElseThrow();
            assertThat(updatedMember.getMannerScore()).isEqualTo(mannerKeywordIds.size());

            // event로 인해 알림 1개가 저장되었는지 검증
            await().atMost(2, TimeUnit.SECONDS).untilAsserted(() -> {
                verify(notificationService, times(1)).createMannerRatingNotification(any(), any(Member.class));
            });
        }

        @DisplayName("성공: 매너 평가 등록으로 인해 매너 레벨이 상승한 경우, 매너 레벨 업데이트 및 알림이 생성되어야 한다.")
        @Test
        void insertPositiveMannerRatingSucceedsWhenMannerLevelUp() {
            // given
            List<Long> mannerKeywordIds = List.of(1L, 2L, 3L);
            MannerInsertRequest request = MannerInsertRequest.builder()
                    .mannerKeywordIdList(mannerKeywordIds)
                    .build();

            targetMember.updateMannerScore(8);
            memberRepository.save(targetMember);

            // when
            mannerFacadeService.insertPositiveMannerRating(member, targetMember.getId(), request);

            // then
            // targetMember의 매너 점수 업데이트 검증
            Member updatedMember = memberRepository.findById(targetMember.getId()).orElseThrow();
            assertThat(updatedMember.getMannerScore()).isEqualTo(11);

            // targetMember의 매너 레벨 업데이트 검증
            assertThat(updatedMember.getMannerLevel()).isEqualTo(2);

            // event로 인해 알림 2개가 저장되었는지 검증
            await().atMost(2, TimeUnit.SECONDS).untilAsserted(() -> {
                verify(notificationService, times(1)).createMannerRatingNotification(any(), any(Member.class));
            });
            await().atMost(2, TimeUnit.SECONDS).untilAsserted(() -> {
                verify(notificationService, times(1)).createMannerLevelNotification(
                        eq(NotificationTypeTitle.MANNER_LEVEL_UP), any(Member.class), eq(2));
            });

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

}
