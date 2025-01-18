package com.gamegoo.gamegoo_v2.service.manner;

import com.gamegoo.gamegoo_v2.account.member.domain.LoginType;
import com.gamegoo.gamegoo_v2.account.member.domain.Member;
import com.gamegoo.gamegoo_v2.account.member.domain.Tier;
import com.gamegoo.gamegoo_v2.account.member.repository.MemberRepository;
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

    @AfterEach
    void tearDown() {
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

    private void updateMannerScore(Member member, Integer score) {
        member.updateMannerScore(score);
        memberRepository.save(member);
    }

    private void updateMannerRank(Member member, Double rank) {
        member.updateMannerRank(rank);
        memberRepository.save(member);
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
