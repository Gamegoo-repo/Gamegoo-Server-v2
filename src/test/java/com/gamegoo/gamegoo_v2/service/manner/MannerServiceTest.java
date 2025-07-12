package com.gamegoo.gamegoo_v2.service.manner;

import com.gamegoo.gamegoo_v2.account.member.domain.LoginType;
import com.gamegoo.gamegoo_v2.account.member.domain.Member;
import com.gamegoo.gamegoo_v2.account.member.domain.MemberRecentStats;
import com.gamegoo.gamegoo_v2.account.member.domain.Tier;
import com.gamegoo.gamegoo_v2.account.member.repository.MemberRecentStatsRepository;
import com.gamegoo.gamegoo_v2.account.member.repository.MemberRepository;
import com.gamegoo.gamegoo_v2.social.manner.domain.MannerKeyword;
import com.gamegoo.gamegoo_v2.social.manner.domain.MannerRating;
import com.gamegoo.gamegoo_v2.social.manner.domain.MannerRatingKeyword;
import com.gamegoo.gamegoo_v2.social.manner.repository.MannerKeywordRepository;
import com.gamegoo.gamegoo_v2.social.manner.repository.MannerRatingKeywordRepository;
import com.gamegoo.gamegoo_v2.social.manner.repository.MannerRatingRepository;
import com.gamegoo.gamegoo_v2.social.manner.service.MannerService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SpringBootTest
public class MannerServiceTest {

    @Autowired
    MannerService mannerService;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    MannerKeywordRepository mannerKeywordRepository;

    @Autowired
    MannerRatingRepository mannerRatingRepository;

    @Autowired
    MannerRatingKeywordRepository mannerRatingKeywordRepository;

    @Autowired
    MemberRecentStatsRepository memberRecentStatsRepository;

    @AfterEach
    void tearDown() {
        memberRecentStatsRepository.deleteAll();
        mannerRatingKeywordRepository.deleteAllInBatch();
        mannerRatingRepository.deleteAllInBatch();
        memberRepository.deleteAllInBatch();
    }

    @Nested
    @DisplayName("매너 랭크 업데이트 대상 회원 조회")
    class GetMannerRankUpdateTargetsTest {

        @DisplayName("대상 회원이 없는 경우")
        @Test
        void getMannerRankUpdateTargetsSucceedsWhenNoTarget() {
            // given
            createMember("member1@gmail.com", "member1");

            // when
            List<Long> memberIds = mannerService.getMannerRankUpdateTargets();

            // then
            assertThat(memberIds).isEmpty();
        }

        @DisplayName("대상 회원이 있는 경우")
        @Test
        void getMannerRankUpdateTargetsSucceeds() {
            // given
            List<Long> memberIds = new ArrayList<>();
            for (int i = 0; i < 10; i++) {
                Member member = createMember("member@email", "member" + i);
                updateMannerScore(member, i + 1);
                memberIds.add(member.getId());
            }

            // when
            List<Long> resultIds = mannerService.getMannerRankUpdateTargets();

            // then
            Collections.reverse(memberIds);
            assertThat(resultIds).isEqualTo(memberIds);
        }

    }

    @Nested
    @DisplayName("매너 랭크 초기화 대상 회원 조회")
    class GetMannerRankResetTargetsTest {

        @DisplayName("대상 회원이 없는 경우")
        @Test
        void getMannerRankResetTargetsSucceedsWhenNoTarget() {
            // given
            Member member1 = createMember("member1@gmail.com", "member1");
            updateMannerScore(member1, 1);
            createMember("member2@gmail.com", "member2");

            // when
            List<Long> memberIds = mannerService.getMannerRankResetTargets();

            // then
            assertThat(memberIds).isEmpty();
        }

        @DisplayName("대상 회원이 있는 경우")
        @Test
        void getMannerRankResetTargetsSucceeds() {
            // given
            Member member1 = createMember("member1@gmail.com", "member1");
            updateMannerRank(member1, 1.0);

            // when
            List<Long> memberIds = mannerService.getMannerRankResetTargets();

            // then
            assertThat(memberIds).hasSize(1);
            assertThat(memberIds.get(0)).isEqualTo(member1.getId());
        }

    }

    @Nested
    @DisplayName("해당 회원이 등록한 모든 매너/비매너 평가 삭제")
    class DeleteAllMannerRatingsByMemberTest {

        @DisplayName("해당 회원이 등록한 매너/비매너 평가가 없는 경우")
        @Test
        void deleteAllMannerRatingsByMemberWhenNoTarget() {
            // given
            Member member = createMember("member@gmail.com", "member");

            // when
            mannerService.deleteAllMannerRatingsByMember(member);

            // then
            assertThat(mannerRatingRepository.findAllByFromMember(member)).isEmpty();
        }

        @DisplayName("해당 회원이 등록한 매너 평가가 있는 경우")
        @Test
        void deleteAllMannerRatingsByMemberWhenPositiveRatings() {
            // given
            Member member = createMember("member@gmail.com", "member");
            Member targetMember = createMember("target@gmail.com", "target");

            // mannerRating 생성
            MannerRating mannerRating = mannerRatingRepository.save(MannerRating.create(member, targetMember, true));

            // MannerRatingKeyword 생성
            List<Long> mannerKeywordIds = List.of(1L, 2L, 3L);
            List<MannerKeyword> mannerKeywordList = mannerKeywordRepository.findAllById(mannerKeywordIds);
            for (MannerKeyword mannerKeyword : mannerKeywordList) {
                mannerRatingKeywordRepository.save(MannerRatingKeyword.create(mannerRating, mannerKeyword));
            }

            // targetMember 정보 업데이트
            Integer score = targetMember.updateMannerScore(mannerKeywordIds.size() + 9);
            targetMember.updateMannerLevel(score);
            memberRepository.save(targetMember);

            // when
            mannerService.deleteAllMannerRatingsByMember(member);

            // then
            // targetMember의 매너 점수 업데이트 검증
            Member updatedMember = memberRepository.findById(targetMember.getId()).orElseThrow();
            assertThat(updatedMember.getMannerScore()).isEqualTo(9);

            // targetMember의 매너 레벨 업데이트 검증
            assertThat(updatedMember.getMannerLevel()).isEqualTo(1);

            // MannerRating 삭제 검증
            List<MannerRating> mannerRatings = mannerRatingRepository.findAllByFromMember(member);
            assertThat(mannerRatings).isEmpty();

            // MannerRatingKeyword 삭제 검증
            List<MannerRatingKeyword> mannerRatingKeywords = mannerRatingKeywordRepository.findAll();
            assertThat(mannerRatingKeywords).isEmpty();
        }

        @DisplayName("해당 회원이 등록한 비매너 평가가 있는 경우")
        @Test
        void deleteAllMannerRatingsByMemberWhenNegativeRatings() {
            // given
            Member member = createMember("member@gmail.com", "member");
            Member targetMember = createMember("target@gmail.com", "target");

            MannerRating mannerRating = mannerRatingRepository.save(MannerRating.create(member, targetMember, false));

            // MannerRatingKeyword 생성
            List<Long> mannerKeywordIds = List.of(7L, 8L, 9L);
            List<MannerKeyword> mannerKeywordList = mannerKeywordRepository.findAllById(mannerKeywordIds);
            for (MannerKeyword mannerKeyword : mannerKeywordList) {
                mannerRatingKeywordRepository.save(MannerRatingKeyword.create(mannerRating, mannerKeyword));
            }

            // targetMember 정보 업데이트
            Integer score = targetMember.updateMannerScore(-2 * mannerKeywordIds.size() + 10);
            targetMember.updateMannerLevel(score);
            memberRepository.save(targetMember);

            // when
            mannerService.deleteAllMannerRatingsByMember(member);

            // then
            // targetMember의 매너 점수 업데이트 검증
            Member updatedMember = memberRepository.findById(targetMember.getId()).orElseThrow();
            assertThat(updatedMember.getMannerScore()).isEqualTo(10);

            // targetMember의 매너 레벨 업데이트 검증
            assertThat(updatedMember.getMannerLevel()).isEqualTo(2);

            // MannerRating 삭제 검증
            List<MannerRating> mannerRatings = mannerRatingRepository.findAllByFromMember(member);
            assertThat(mannerRatings).isEmpty();

            // MannerRatingKeyword 삭제 검증
            List<MannerRatingKeyword> mannerRatingKeywords = mannerRatingKeywordRepository.findAll();
            assertThat(mannerRatingKeywords).isEmpty();
        }

    }

    private void updateMannerScore(Member member, Integer score) {
        member.updateMannerScore(score);
        memberRepository.save(member);
    }

    private void updateMannerRank(Member member, Double rank) {
        member.updateMannerRank(rank);
        memberRepository.save(member);
    }

    private Member createMember(String email, String gameName) {
        Member member = Member.builder()
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
                .build();

        memberRecentStatsRepository.save(MemberRecentStats.builder()
                .member(member)
                .build());

        return memberRepository.save(member);
    }

}
