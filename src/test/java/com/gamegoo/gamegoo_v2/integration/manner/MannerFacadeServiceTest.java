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
import com.gamegoo.gamegoo_v2.social.manner.domain.MannerKeyword;
import com.gamegoo.gamegoo_v2.social.manner.domain.MannerRating;
import com.gamegoo.gamegoo_v2.social.manner.domain.MannerRatingKeyword;
import com.gamegoo.gamegoo_v2.social.manner.dto.request.MannerInsertRequest;
import com.gamegoo.gamegoo_v2.social.manner.dto.request.MannerUpdateRequest;
import com.gamegoo.gamegoo_v2.social.manner.dto.response.MannerInsertResponse;
import com.gamegoo.gamegoo_v2.social.manner.dto.response.MannerRatingResponse;
import com.gamegoo.gamegoo_v2.social.manner.dto.response.MannerResponse;
import com.gamegoo.gamegoo_v2.social.manner.dto.response.MannerUpdateResponse;
import com.gamegoo.gamegoo_v2.social.manner.repository.MannerKeywordRepository;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    private MannerKeywordRepository mannerKeywordRepository;

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

    @Nested
    @DisplayName("비매너 평가 등록")
    class InsertNegativeMannerRatingTest {

        @DisplayName("실패: 대상 회원을 찾을 수 없는 경우 예외가 발생한다.")
        @Test
        void insertNegativeMannerRating_shouldThrownWhenMemberNotFound() {
            // givenL
            List<Long> mannerKeywordIds = List.of(7L, 8L, 9L);
            MannerInsertRequest request = MannerInsertRequest.builder()
                    .mannerKeywordIdList(mannerKeywordIds)
                    .build();

            // when // then
            assertThatThrownBy(() -> mannerFacadeService.insertNegativeMannerRating(member, 1000L, request))
                    .isInstanceOf(MemberException.class)
                    .hasMessage(ErrorCode.MEMBER_NOT_FOUND.getMessage());
        }

        @DisplayName("실패: 매너 키워드 id에 6 이하의 값이 들어간 경우 예외가 발생한다.")
        @Test
        void insertNegativeMannerRating_shouldThrownWhenMannerKeywordIsNotValid() {
            // given
            List<Long> mannerKeywordIds = List.of(1L, 7L, 8L);
            MannerInsertRequest request = MannerInsertRequest.builder()
                    .mannerKeywordIdList(mannerKeywordIds)
                    .build();

            // when // then
            assertThatThrownBy(
                    () -> mannerFacadeService.insertNegativeMannerRating(member, targetMember.getId(), request))
                    .isInstanceOf(MannerException.class)
                    .hasMessage(ErrorCode.MANNER_KEYWORD_INVALID.getMessage());
        }

        @DisplayName("실패: 매너 키워드 id에 13 이상의 값이 들어간 경우 예외가 발생한다.")
        @Test
        void insertNegativeMannerRating_shouldThrownWhenMannerKeywordIdIsLargerThanMax() {
            // given
            List<Long> mannerKeywordIds = List.of(7L, 8L, 15L);
            MannerInsertRequest request = MannerInsertRequest.builder()
                    .mannerKeywordIdList(mannerKeywordIds)
                    .build();

            // when // then
            assertThatThrownBy(
                    () -> mannerFacadeService.insertNegativeMannerRating(member, targetMember.getId(), request))
                    .isInstanceOf(MannerException.class)
                    .hasMessage(ErrorCode.MANNER_KEYWORD_INVALID.getMessage());
        }

        @DisplayName("실패: 평가 대상으로 본인 id를 입력한 경우 예외가 발생한다.")
        @Test
        void insertNegativeMannerRating_shouldThrownWhenTargetIsSelf() {
            // given
            List<Long> mannerKeywordIds = List.of(7L, 8L, 9L);
            MannerInsertRequest request = MannerInsertRequest.builder()
                    .mannerKeywordIdList(mannerKeywordIds)
                    .build();

            // when // then
            assertThatThrownBy(() -> mannerFacadeService.insertNegativeMannerRating(member, member.getId(), request))
                    .isInstanceOf(GlobalException.class)
                    .hasMessage(ErrorCode._BAD_REQUEST.getMessage());
        }

        @DisplayName("실패: 대상 회원이 탈퇴한 경우 예외가 발생한다.")
        @Test
        void insertNegativeMannerRating_shouldThrownWhenTargetIsBlind() {
            // given
            List<Long> mannerKeywordIds = List.of(7L, 8L, 9L);
            MannerInsertRequest request = MannerInsertRequest.builder()
                    .mannerKeywordIdList(mannerKeywordIds)
                    .build();

            // 대상 회원 탈퇴 처리
            targetMember.updateBlind(true);
            memberRepository.save(targetMember);

            // when // then
            assertThatThrownBy(
                    () -> mannerFacadeService.insertNegativeMannerRating(member, targetMember.getId(), request))
                    .isInstanceOf(MemberException.class)
                    .hasMessage(ErrorCode.TARGET_MEMBER_DEACTIVATED.getMessage());
        }

        @DisplayName("실패: 대상 회원에게 등록한 비매너 평가가 존재하는 경우 예외가 발생한다.")
        @Test
        void insertNegativeMannerRating_shouldThrownWhenMannerRatingExists() {
            // given
            List<Long> mannerKeywordIds = List.of(7L, 8L, 9L);
            MannerInsertRequest request = MannerInsertRequest.builder()
                    .mannerKeywordIdList(mannerKeywordIds)
                    .build();

            mannerRatingRepository.save(MannerRating.create(member, targetMember, false));

            // when // then
            assertThatThrownBy(
                    () -> mannerFacadeService.insertNegativeMannerRating(member, targetMember.getId(), request))
                    .isInstanceOf(MannerException.class)
                    .hasMessage(ErrorCode.MANNER_RATING_EXISTS.getMessage());
        }

        @DisplayName("성공: 엔티티 생성 및 저장, 매너 평가 등록 알림이 생성되어야 한다.")
        @Test
        void insertNegativeMannerRatingSucceeds() {
            // given
            List<Long> mannerKeywordIds = List.of(7L, 8L, 9L);
            MannerInsertRequest request = MannerInsertRequest.builder()
                    .mannerKeywordIdList(mannerKeywordIds)
                    .build();

            // when
            MannerInsertResponse response = mannerFacadeService.insertNegativeMannerRating(member, targetMember.getId(),
                    request);

            // then
            assertThat(response.getTargetMemberId()).isEqualTo(targetMember.getId());
            assertThat(response.getMannerKeywordIdList()).isEqualTo(mannerKeywordIds);

            // mannerRating 엔티티 검증
            MannerRating mannerRating = mannerRatingRepository.findById(response.getMannerRatingId()).orElseThrow();
            assertThat(mannerRating.getFromMember().getId()).isEqualTo(member.getId());
            assertThat(mannerRating.getToMember().getId()).isEqualTo(targetMember.getId());
            assertThat(mannerRating.isPositive()).isFalse();

            // mannerRatingKeyword 엔티티 검증
            List<MannerRatingKeyword> mannerRatingKeywords = mannerRatingKeywordRepository.findByMannerRatingId(
                    mannerRating.getId());
            assertThat(mannerRatingKeywords).hasSize(mannerKeywordIds.size());

            // member의 매너 점수 업데이트 검증
            Member updatedMember = memberRepository.findById(targetMember.getId()).orElseThrow();
            assertThat(updatedMember.getMannerScore()).isEqualTo(-2 * mannerKeywordIds.size());

            // event로 인해 알림 1개가 저장되었는지 검증
            await().atMost(2, TimeUnit.SECONDS).untilAsserted(() -> {
                verify(notificationService, times(1)).createMannerRatingNotification(any(), any(Member.class));
            });
        }

        @DisplayName("성공: 비매너 평가 등록으로 인해 매너 레벨이 하락한 경우, 매너 레벨 업데이트 및 알림이 생성되어야 한다.")
        @Test
        void insertNegativeMannerRatingSucceedsWhenMannerLevelDown() {
            // given
            List<Long> mannerKeywordIds = List.of(7L, 8L, 9L);
            MannerInsertRequest request = MannerInsertRequest.builder()
                    .mannerKeywordIdList(mannerKeywordIds)
                    .build();

            targetMember.updateMannerScore(11);
            targetMember.updateMannerLevel(2);
            memberRepository.save(targetMember);

            // when
            mannerFacadeService.insertNegativeMannerRating(member, targetMember.getId(), request);

            // then
            // targetMember의 매너 점수 업데이트 검증
            Member updatedMember = memberRepository.findById(targetMember.getId()).orElseThrow();
            assertThat(updatedMember.getMannerScore()).isEqualTo(5);

            // targetMember의 매너 레벨 업데이트 검증
            assertThat(updatedMember.getMannerLevel()).isEqualTo(1);

            // event로 인해 알림 2개가 저장되었는지 검증
            await().atMost(2, TimeUnit.SECONDS).untilAsserted(() -> {
                verify(notificationService, times(1)).createMannerRatingNotification(any(), any(Member.class));
            });
            await().atMost(2, TimeUnit.SECONDS).untilAsserted(() -> {
                verify(notificationService, times(1)).createMannerLevelNotification(
                        eq(NotificationTypeTitle.MANNER_LEVEL_DOWN), any(Member.class), eq(1));
            });
        }
    }

    @Nested
    @DisplayName("매너/비매너 평가 수정")
    class UpdateMannerRatingTest {

        @DisplayName("실패: mannerId에 해당하는 매너 평가를 찾을 수 없는 경우 예외가 발생한다.")
        @Test
        void updateMannerRating_shouldThrownWhenMannerRatingNotFound() {
            // given
            List<Long> newMannerKeywordIds = List.of(1L, 2L);
            MannerUpdateRequest request = MannerUpdateRequest.builder()
                    .mannerKeywordIdList(newMannerKeywordIds)
                    .build();

            // when // then
            assertThatThrownBy(() -> mannerFacadeService.updateMannerRating(member, 1000L, request))
                    .isInstanceOf(MannerException.class)
                    .hasMessage(ErrorCode.MANNER_RATING_NOT_FOUND.getMessage());
        }

        @DisplayName("실패: 해당 매너 평가의 작성자가 본인이 아닌 경우 예외가 발생한다.")
        @Test
        void updateMannerRating_shouldThrownWhenNotMannerRatingOwner() {
            // given
            // 기존 매너 평가 등록
            MannerRating mannerRating = createMannerRating(List.of(1L, 2L, 3L), targetMember, member, true);

            List<Long> newMannerKeywordIds = List.of(1L, 2L);
            MannerUpdateRequest request = MannerUpdateRequest.builder()
                    .mannerKeywordIdList(newMannerKeywordIds)
                    .build();

            // when // then
            assertThatThrownBy(() -> mannerFacadeService.updateMannerRating(member, mannerRating.getId(), request))
                    .isInstanceOf(MannerException.class)
                    .hasMessage(ErrorCode.MANNER_RATING_ACCESS_DENIED.getMessage());
        }

        @DisplayName("실패: 매너 평가 수정 요청으로 비매너 키워드 id를 요청한 경우 예외가 발생한다.")
        @Test
        void updateMannerRating_shouldThrownWhenPositiveMannerKeywordNotValid() {
            // given
            // 기존 매너 평가 등록
            MannerRating mannerRating = createMannerRating(List.of(1L, 2L, 3L), member, targetMember, true);

            List<Long> newMannerKeywordIds = List.of(7L, 8L);
            MannerUpdateRequest request = MannerUpdateRequest.builder()
                    .mannerKeywordIdList(newMannerKeywordIds)
                    .build();

            // when // then
            assertThatThrownBy(() -> mannerFacadeService.updateMannerRating(member, mannerRating.getId(), request))
                    .isInstanceOf(MannerException.class)
                    .hasMessage(ErrorCode.MANNER_KEYWORD_INVALID.getMessage());
        }

        @DisplayName("실패: 비매너 평가 수정 요청으로 매너 키워드 id를 요청한 경우 예외가 발생한다.")
        @Test
        void updateMannerRating_shouldThrownWhenNegativeMannerKeywordNotValid() {
            // given
            // 기존 매너 평가 등록
            MannerRating mannerRating = createMannerRating(List.of(7L, 8L, 9L), member, targetMember, false);

            List<Long> newMannerKeywordIds = List.of(1L, 2L);
            MannerUpdateRequest request = MannerUpdateRequest.builder()
                    .mannerKeywordIdList(newMannerKeywordIds)
                    .build();

            // when // then
            assertThatThrownBy(() -> mannerFacadeService.updateMannerRating(member, mannerRating.getId(), request))
                    .isInstanceOf(MannerException.class)
                    .hasMessage(ErrorCode.MANNER_KEYWORD_INVALID.getMessage());
        }

        @DisplayName("실패: 상대가 탈퇴한 경우 예외가 발생한다")
        @Test
        void updateMannerRating_shouldThrownWhenTargetIsBlind() {
            // given
            MannerRating mannerRating = createMannerRating(List.of(1L, 2L, 3L), member, targetMember, true);

            List<Long> newMannerKeywordIds = List.of(1L, 2L);
            MannerUpdateRequest request = MannerUpdateRequest.builder()
                    .mannerKeywordIdList(newMannerKeywordIds)
                    .build();

            // 대상 회원 탈퇴 처리
            targetMember.updateBlind(true);
            memberRepository.save(targetMember);

            // when // then
            assertThatThrownBy(() -> mannerFacadeService.updateMannerRating(member, mannerRating.getId(), request))
                    .isInstanceOf(MemberException.class)
                    .hasMessage(ErrorCode.TARGET_MEMBER_DEACTIVATED.getMessage());
        }

        @DisplayName("성공: 매너 평가 키워드 개수가 줄어든 경우")
        @Test
        void updateMannerRatingSucceedsWhenPositiveKeywordDecrease() {
            // given
            // 기존 매너 평가 등록
            MannerRating mannerRating = createMannerRating(List.of(1L, 2L, 3L), member, targetMember, true);

            List<Long> newMannerKeywordIds = List.of(1L, 2L);
            MannerUpdateRequest request = MannerUpdateRequest.builder()
                    .mannerKeywordIdList(newMannerKeywordIds)
                    .build();

            targetMember.updateMannerScore(10);
            targetMember.updateMannerLevel(2);
            memberRepository.save(targetMember);

            // when
            MannerUpdateResponse response = mannerFacadeService.updateMannerRating(member, mannerRating.getId(),
                    request);

            // then
            assertThat(response.getTargetMemberId()).isEqualTo(targetMember.getId());
            assertThat(response.getMannerKeywordIdList()).hasSize(newMannerKeywordIds.size());
            assertThat(response.getMannerRatingId()).isEqualTo(mannerRating.getId());

            // mannerRatingKeyword 엔티티 검증
            List<MannerRatingKeyword> mannerRatingKeywords = mannerRatingKeywordRepository.findByMannerRatingId(
                    mannerRating.getId());
            assertThat(mannerRatingKeywords).hasSize(newMannerKeywordIds.size());
            mannerRatingKeywords.forEach(mrk -> {
                assertThat(mrk.getMannerKeyword().getId()).isIn(newMannerKeywordIds);
            });

            // 매너 점수 및 레벨 업데이트 검증
            Member updatedMember = memberRepository.findById(targetMember.getId()).orElseThrow();
            assertThat(updatedMember.getMannerScore()).isEqualTo(9);
            assertThat(updatedMember.getMannerLevel()).isEqualTo(1);

            // event로 인해 알림 1개가 저장되었는지 검증
            await().atMost(2, TimeUnit.SECONDS).untilAsserted(() -> {
                verify(notificationService, times(1)).createMannerLevelNotification(
                        eq(NotificationTypeTitle.MANNER_LEVEL_DOWN), any(Member.class), eq(1));
            });
        }

        @DisplayName("성공: 매너 평가 키워드 개수가 늘어난 경우")
        @Test
        void updateMannerRatingSucceedsWhenPositiveKeywordIncrease() {
            // given
            // 기존 매너 평가 등록
            MannerRating mannerRating = createMannerRating(List.of(1L, 2L, 3L), member, targetMember, true);

            List<Long> newMannerKeywordIds = List.of(1L, 2L, 3L, 4L);
            MannerUpdateRequest request = MannerUpdateRequest.builder()
                    .mannerKeywordIdList(newMannerKeywordIds)
                    .build();

            targetMember.updateMannerScore(9);
            targetMember.updateMannerLevel(1);
            memberRepository.save(targetMember);

            // when
            MannerUpdateResponse response = mannerFacadeService.updateMannerRating(member, mannerRating.getId(),
                    request);

            // then
            assertThat(response.getTargetMemberId()).isEqualTo(targetMember.getId());
            assertThat(response.getMannerKeywordIdList()).hasSize(newMannerKeywordIds.size());
            assertThat(response.getMannerRatingId()).isEqualTo(mannerRating.getId());

            // mannerRatingKeyword 엔티티 검증
            List<MannerRatingKeyword> mannerRatingKeywords = mannerRatingKeywordRepository.findByMannerRatingId(
                    mannerRating.getId());
            assertThat(mannerRatingKeywords).hasSize(newMannerKeywordIds.size());
            mannerRatingKeywords.forEach(mrk -> {
                assertThat(mrk.getMannerKeyword().getId()).isIn(newMannerKeywordIds);
            });

            // 매너 점수 및 레벨 업데이트 검증
            Member updatedMember = memberRepository.findById(targetMember.getId()).orElseThrow();
            assertThat(updatedMember.getMannerScore()).isEqualTo(10);
            assertThat(updatedMember.getMannerLevel()).isEqualTo(2);

            // event로 인해 알림 1개가 저장되었는지 검증
            await().atMost(2, TimeUnit.SECONDS).untilAsserted(() -> {
                verify(notificationService, times(1)).createMannerLevelNotification(
                        eq(NotificationTypeTitle.MANNER_LEVEL_UP), any(Member.class), eq(2));
            });
        }

        @DisplayName("성공: 매너 평가 키워드 개수는 같지만 id가 전부 달라진 경우")
        @Test
        void updateMannerRatingSucceedsWhenPositiveKeyword() {
            // given
            // 기존 매너 평가 등록
            MannerRating mannerRating = createMannerRating(List.of(1L, 2L, 3L), member, targetMember, true);

            List<Long> newMannerKeywordIds = List.of(4L, 5L, 6L);
            MannerUpdateRequest request = MannerUpdateRequest.builder()
                    .mannerKeywordIdList(newMannerKeywordIds)
                    .build();

            targetMember.updateMannerScore(3);
            targetMember.updateMannerLevel(1);
            memberRepository.save(targetMember);

            // when
            MannerUpdateResponse response = mannerFacadeService.updateMannerRating(member, mannerRating.getId(),
                    request);

            // then
            assertThat(response.getTargetMemberId()).isEqualTo(targetMember.getId());
            assertThat(response.getMannerKeywordIdList()).hasSize(newMannerKeywordIds.size());
            assertThat(response.getMannerRatingId()).isEqualTo(mannerRating.getId());

            // mannerRatingKeyword 엔티티 검증
            List<MannerRatingKeyword> mannerRatingKeywords = mannerRatingKeywordRepository.findByMannerRatingId(
                    mannerRating.getId());
            assertThat(mannerRatingKeywords).hasSize(newMannerKeywordIds.size());
            mannerRatingKeywords.forEach(mrk -> {
                assertThat(mrk.getMannerKeyword().getId()).isIn(newMannerKeywordIds);
            });

            // 매너 점수 및 레벨 업데이트 검증
            Member updatedMember = memberRepository.findById(targetMember.getId()).orElseThrow();
            assertThat(updatedMember.getMannerScore()).isEqualTo(3);
            assertThat(updatedMember.getMannerLevel()).isEqualTo(1);

            // 알림 event 발생 안함 검증
            await().atMost(2, TimeUnit.SECONDS).untilAsserted(() -> {
                verify(notificationService, times(0)).createMannerLevelNotification(any(), any(Member.class), any());
            });
        }

        @DisplayName("성공: 비매너 평가 키워드 개수가 줄어든 경우")
        @Test
        void updateMannerRatingSucceedsWhenNegativeKeywordDecrease() {
            // given
            // 기존 매너 평가 등록
            MannerRating mannerRating = createMannerRating(List.of(7L, 8L, 9L), member, targetMember, false);

            List<Long> newMannerKeywordIds = List.of(7L, 8L);
            MannerUpdateRequest request = MannerUpdateRequest.builder()
                    .mannerKeywordIdList(newMannerKeywordIds)
                    .build();

            targetMember.updateMannerScore(9);
            targetMember.updateMannerLevel(1);
            memberRepository.save(targetMember);

            // when
            MannerUpdateResponse response = mannerFacadeService.updateMannerRating(member, mannerRating.getId(),
                    request);

            // then
            assertThat(response.getTargetMemberId()).isEqualTo(targetMember.getId());
            assertThat(response.getMannerKeywordIdList()).hasSize(newMannerKeywordIds.size());
            assertThat(response.getMannerRatingId()).isEqualTo(mannerRating.getId());

            // mannerRatingKeyword 엔티티 검증
            List<MannerRatingKeyword> mannerRatingKeywords = mannerRatingKeywordRepository.findByMannerRatingId(
                    mannerRating.getId());
            assertThat(mannerRatingKeywords).hasSize(newMannerKeywordIds.size());
            mannerRatingKeywords.forEach(mrk -> {
                assertThat(mrk.getMannerKeyword().getId()).isIn(newMannerKeywordIds);
            });

            // 매너 점수 및 레벨 업데이트 검증
            Member updatedMember = memberRepository.findById(targetMember.getId()).orElseThrow();
            assertThat(updatedMember.getMannerScore()).isEqualTo(10);
            assertThat(updatedMember.getMannerLevel()).isEqualTo(2);

            // event로 인해 알림 1개가 저장되었는지 검증
            await().atMost(2, TimeUnit.SECONDS).untilAsserted(() -> {
                verify(notificationService, times(1)).createMannerLevelNotification(
                        eq(NotificationTypeTitle.MANNER_LEVEL_UP), any(Member.class), eq(2));
            });
        }

        @DisplayName("성공: 비매너 평가 키워드 개수가 늘어난 경우")
        @Test
        void updateMannerRatingSucceedsWhenNegativeKeywordIncrease() {
            // given
            // 기존 매너 평가 등록
            MannerRating mannerRating = createMannerRating(List.of(7L, 8L), member, targetMember, false);

            List<Long> newMannerKeywordIds = List.of(9L, 10L, 11L);
            MannerUpdateRequest request = MannerUpdateRequest.builder()
                    .mannerKeywordIdList(newMannerKeywordIds)
                    .build();

            targetMember.updateMannerScore(10);
            targetMember.updateMannerLevel(2);
            memberRepository.save(targetMember);

            // when
            MannerUpdateResponse response = mannerFacadeService.updateMannerRating(member, mannerRating.getId(),
                    request);

            // then
            assertThat(response.getTargetMemberId()).isEqualTo(targetMember.getId());
            assertThat(response.getMannerKeywordIdList()).hasSize(newMannerKeywordIds.size());
            assertThat(response.getMannerRatingId()).isEqualTo(mannerRating.getId());

            // mannerRatingKeyword 엔티티 검증
            List<MannerRatingKeyword> mannerRatingKeywords = mannerRatingKeywordRepository.findByMannerRatingId(
                    mannerRating.getId());
            assertThat(mannerRatingKeywords).hasSize(newMannerKeywordIds.size());
            mannerRatingKeywords.forEach(mrk -> {
                assertThat(mrk.getMannerKeyword().getId()).isIn(newMannerKeywordIds);
            });

            // 매너 점수 및 레벨 업데이트 검증
            Member updatedMember = memberRepository.findById(targetMember.getId()).orElseThrow();
            assertThat(updatedMember.getMannerScore()).isEqualTo(9);
            assertThat(updatedMember.getMannerLevel()).isEqualTo(1);

            // event로 인해 알림 1개가 저장되었는지 검증
            await().atMost(2, TimeUnit.SECONDS).untilAsserted(() -> {
                verify(notificationService, times(1)).createMannerLevelNotification(
                        eq(NotificationTypeTitle.MANNER_LEVEL_DOWN), any(Member.class), eq(1));
            });
        }

        @DisplayName("성공: 비매너 평가 키워드 개수는 같지만 id가 전부 달라진 경우")
        @Test
        void updateMannerRatingSucceedsWhenNegativeKeyword() {
            // given
            // 기존 매너 평가 등록
            MannerRating mannerRating = createMannerRating(List.of(7L, 8L, 9L), member, targetMember, false);

            List<Long> newMannerKeywordIds = List.of(10L, 11L, 12L);
            MannerUpdateRequest request = MannerUpdateRequest.builder()
                    .mannerKeywordIdList(newMannerKeywordIds)
                    .build();

            targetMember.updateMannerScore(-6);
            targetMember.updateMannerLevel(1);
            memberRepository.save(targetMember);

            // when
            MannerUpdateResponse response = mannerFacadeService.updateMannerRating(member, mannerRating.getId(),
                    request);

            // then
            assertThat(response.getTargetMemberId()).isEqualTo(targetMember.getId());
            assertThat(response.getMannerKeywordIdList()).hasSize(newMannerKeywordIds.size());
            assertThat(response.getMannerRatingId()).isEqualTo(mannerRating.getId());

            // mannerRatingKeyword 엔티티 검증
            List<MannerRatingKeyword> mannerRatingKeywords = mannerRatingKeywordRepository.findByMannerRatingId(
                    mannerRating.getId());
            assertThat(mannerRatingKeywords).hasSize(newMannerKeywordIds.size());
            mannerRatingKeywords.forEach(mrk -> {
                assertThat(mrk.getMannerKeyword().getId()).isIn(newMannerKeywordIds);
            });

            // 매너 점수 및 레벨 업데이트 검증
            Member updatedMember = memberRepository.findById(targetMember.getId()).orElseThrow();
            assertThat(updatedMember.getMannerScore()).isEqualTo(-6);
            assertThat(updatedMember.getMannerLevel()).isEqualTo(1);

            // 알림 event 발생 안함 검증
            await().atMost(2, TimeUnit.SECONDS).untilAsserted(() -> {
                verify(notificationService, times(0)).createMannerLevelNotification(any(), any(Member.class), any());
            });
        }
    }

    @Nested
    @DisplayName("특정 회원에 대한 매너/비매너 평가 조회")
    class GetMannerRatingTest {

        @DisplayName("실패: 대상 회원을 찾을 수 없는 경우 예외가 발생한다.")
        @Test
        void getMannerRating_shouldThrownWhenTargetIsNotFound() {
            // when // then
            assertThatThrownBy(() -> mannerFacadeService.getMannerRating(member, 200L, true))
                    .isInstanceOf(MemberException.class)
                    .hasMessage(ErrorCode.MEMBER_NOT_FOUND.getMessage());
        }

        @DisplayName("성공: 매너 평가 내역이 존재하는 경우")
        @Test
        void getMannerRatingSucceedsWhenPositiveExist() {
            // given
            List<Long> mannerKeywordIds = List.of(1L, 2L, 3L);
            MannerRating mannerRating = createMannerRating(mannerKeywordIds, member, targetMember, true);

            // when
            MannerRatingResponse response = mannerFacadeService.getMannerRating(member, targetMember.getId(), true);

            // then
            assertThat(response.getMannerRatingId()).isEqualTo(mannerRating.getId());
            assertThat(response.getMannerKeywordIdList()).hasSize(3);
            response.getMannerKeywordIdList().forEach(mannerKeywordId ->
                    assertThat(mannerKeywordId).isIn(mannerKeywordIds));
        }

        @DisplayName("성공: 매너 평가 내역이 존재하지 않는 경우")
        @Test
        void getMannerRatingSucceedsWhenPositiveNotExist() {
            //given
            List<Long> mannerKeywordIds = List.of(7L, 8L, 9L);
            createMannerRating(mannerKeywordIds, member, targetMember, false);

            // when
            MannerRatingResponse response = mannerFacadeService.getMannerRating(member, targetMember.getId(), true);

            // then
            assertThat(response.getMannerRatingId()).isNull();
            assertThat(response.getMannerKeywordIdList()).isEmpty();
        }

        @DisplayName("성공: 비매너 평가 내역이 존재하는 경우")
        @Test
        void getMannerRatingSucceedsWhenNegativeExist() {
            // given
            List<Long> mannerKeywordIds = List.of(7L, 8L, 9L);
            MannerRating mannerRating = createMannerRating(mannerKeywordIds, member, targetMember, false);

            // when
            MannerRatingResponse response = mannerFacadeService.getMannerRating(member, targetMember.getId(), false);

            // then
            assertThat(response.getMannerRatingId()).isEqualTo(mannerRating.getId());
            assertThat(response.getMannerKeywordIdList()).hasSize(3);
            response.getMannerKeywordIdList().forEach(mannerKeywordId ->
                    assertThat(mannerKeywordId).isIn(mannerKeywordIds));
        }

        @DisplayName("성공: 비매너 평가 내역이 존재하지 않는 경우")
        @Test
        void getMannerRatingSucceedsWhenNegativeNotExist() {
            //given
            List<Long> mannerKeywordIds = List.of(1L);
            createMannerRating(mannerKeywordIds, member, targetMember, true);

            // when
            MannerRatingResponse response = mannerFacadeService.getMannerRating(member, targetMember.getId(), false);

            // then
            assertThat(response.getMannerRatingId()).isNull();
            assertThat(response.getMannerKeywordIdList()).isEmpty();
        }
    }

    @Nested
    @DisplayName("회원 매너 정보 조회")
    class GetMannerInfoTest {

        @DisplayName("성공: 받은 매너 평가가 없는 경우")
        @Test
        void getMannerInfoSucceedsWhenNoMannerRating() {
            // when
            MannerResponse response = mannerFacadeService.getMannerInfo(member);

            // then
            assertThat(response.getMannerLevel()).isEqualTo(1);
            assertThat(response.getMannerRank()).isNull();
            assertThat(response.getMannerRatingCount()).isEqualTo(0);
            assertThat(response.getMannerKeywords())
                    .isNotNull()
                    .hasSize(12)
                    .allSatisfy(mannerKeywordResponse -> assertThat(mannerKeywordResponse.getCount()).isEqualTo(0));
        }

        @DisplayName("성공: 받은 매너 평가가 있는 경우")
        @Test
        void getMannerInfoSucceeds() {
            // given
            Member targetMember1 = createMember("targetMember1@gmail.com", "targetMember1");
            Member targetMember2 = createMember("targetMember2@gmail.com", "targetMember2");
            Member targetMember3 = createMember("targetMember3@gmail.com", "targetMember3");

            createMannerRating(List.of(1L, 2L, 3L, 4L, 5L, 6L), targetMember1, member, true);
            createMannerRating(List.of(1L, 2L, 3L), targetMember2, member, true);
            createMannerRating(List.of(7L, 8L), targetMember3, member, false);

            member.updateMannerScore(5);
            member.updateMannerRank(50.0);

            // when
            MannerResponse response = mannerFacadeService.getMannerInfo(member);

            // then
            assertThat(response.getMannerLevel()).isEqualTo(1);
            assertThat(response.getMannerRank()).isEqualTo(50.0);
            assertThat(response.getMannerRatingCount()).isEqualTo(2);

            Map<Long, Integer> assertMap = new HashMap<>();
            assertMap.put(1L, 2);
            assertMap.put(2L, 2);
            assertMap.put(3L, 2);
            assertMap.put(4L, 1);
            assertMap.put(5L, 1);
            assertMap.put(6L, 1);
            assertMap.put(7L, 1);
            assertMap.put(8L, 1);
            assertMap.put(9L, 0);
            assertMap.put(10L, 0);
            assertMap.put(11L, 0);
            assertMap.put(12L, 0);

            assertThat(response.getMannerKeywords())
                    .hasSize(assertMap.size())
                    .allMatch(mannerKeywordResponse ->
                            assertMap.get(mannerKeywordResponse.getMannerKeywordId())
                                    .equals(mannerKeywordResponse.getCount()));
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

    private MannerRating createMannerRating(List<Long> mannerKeywordIds, Member member, Member targetMember,
                                            boolean positive) {
        // 매너 키워드 엔티티 조회
        List<MannerKeyword> mannerKeywordList = mannerKeywordRepository.findAllById(mannerKeywordIds);

        // MannerRating 엔티티 생성 및 저장
        MannerRating mannerRating = mannerRatingRepository.save(MannerRating.create(member, targetMember, positive));

        // MannerRatingKeyword 엔티티 생성 및 저장
        List<MannerRatingKeyword> mannerRatingKeywordList = mannerKeywordList.stream()
                .map(mannerKeyword -> MannerRatingKeyword.create(mannerRating, mannerKeyword))
                .toList();
        mannerRatingKeywordRepository.saveAll(mannerRatingKeywordList);

        return mannerRating;
    }

}
