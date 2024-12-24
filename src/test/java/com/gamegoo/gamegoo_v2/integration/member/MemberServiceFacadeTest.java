package com.gamegoo.gamegoo_v2.integration.member;

import com.gamegoo.gamegoo_v2.account.member.domain.LoginType;
import com.gamegoo.gamegoo_v2.account.member.domain.Member;
import com.gamegoo.gamegoo_v2.account.member.domain.MemberChampion;
import com.gamegoo.gamegoo_v2.account.member.domain.Tier;
import com.gamegoo.gamegoo_v2.account.member.dto.request.IsMikeRequest;
import com.gamegoo.gamegoo_v2.account.member.dto.request.PositionRequest;
import com.gamegoo.gamegoo_v2.account.member.dto.request.ProfileImageRequest;
import com.gamegoo.gamegoo_v2.account.member.dto.response.MyProfileResponse;
import com.gamegoo.gamegoo_v2.account.member.dto.response.OtherProfileResponse;
import com.gamegoo.gamegoo_v2.account.member.repository.MemberChampionRepository;
import com.gamegoo.gamegoo_v2.account.member.repository.MemberRepository;
import com.gamegoo.gamegoo_v2.account.member.service.MemberFacadeService;
import com.gamegoo.gamegoo_v2.core.exception.RiotException;
import com.gamegoo.gamegoo_v2.core.exception.common.ErrorCode;
import com.gamegoo.gamegoo_v2.game.domain.Champion;
import com.gamegoo.gamegoo_v2.game.repository.ChampionRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
public class MemberServiceFacadeTest {

    @Autowired
    MemberFacadeService memberFacadeService;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    ChampionRepository championRepository;

    @Autowired
    MemberChampionRepository memberChampionRepository;

    private static Member member;
    private static Member targetMember;

    private static Champion annie;
    private static Champion olaf;
    private static Champion galio;


    @BeforeEach
    void setUp() {
        // Member 테스트용 객체 생성
        member = createMember("test1@gmail.com", "test1");
        targetMember = createMember("test2@gmail.com", "test2");

        // Champion 테스트용 객체 생성
        annie = initChampion(1L, "Annie");
        olaf = initChampion(2L, "Olaf");
        galio = initChampion(3L, "Galio");

        List<Long> championIds = Arrays.asList(annie.getId(), olaf.getId(), galio.getId());
        initMemberChampion(member, championIds);
        initMemberChampion(targetMember, championIds);
    }

    @AfterEach
    void tearDown() {
        memberChampionRepository.deleteAllInBatch();
        championRepository.deleteAllInBatch();
        memberRepository.deleteAllInBatch();
    }

    @DisplayName("내 프로필 조회 성공")
    @Test
    void getProfile() {
        // when
        MyProfileResponse response = memberFacadeService.getMyProfile(member);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(member.getId());
        assertThat(response.getProfileImg()).isEqualTo(member.getProfileImage());
        assertThat(response.getMike()).isEqualTo(member.isMike());
        assertThat(response.getEmail()).isEqualTo(member.getEmail());
        assertThat(response.getGameName()).isEqualTo(member.getGameName());
        assertThat(response.getTag()).isEqualTo(member.getTag());
        assertThat(response.getTier()).isEqualTo(member.getTier());
        assertThat(response.getGameRank()).isEqualTo(member.getGameRank());
        assertThat(response.getWinrate()).isEqualTo(member.getWinRate());
        assertThat(response.getMainP()).isEqualTo(member.getMainPosition());
        assertThat(response.getSubP()).isEqualTo(member.getSubPosition());
        assertThat(response.getWantP()).isEqualTo(member.getWantPosition());
        assertThat(response.getIsAgree()).isEqualTo(member.isAgree());
        assertThat(response.getIsBlind()).isEqualTo(member.isBlind());
        assertThat(response.getLoginType()).isEqualTo(member.getLoginType().name());
        assertThat(response.getMannerLevel()).isEqualTo(member.getMannerLevel());
        assertThat(response.getChampionResponseList()).isNotNull();

        List<Champion> championList =
                targetMember.getMemberChampionList().stream().map(MemberChampion::getChampion).toList();
        List<Long> championIds = championList.stream().map(Champion::getId).toList();

        for (int i = 0; i < championIds.size(); i++) {
            assertThat(response.getChampionResponseList().get(i).getChampionId()).isEqualTo(championIds.get(i));
        }
    }

    @DisplayName("다른 사람 프로필 조회 성공")
    @Test
    void getOtherProfile() {
        // when
        OtherProfileResponse response = memberFacadeService.getOtherProfile(member, targetMember.getId());

        // then
        // TODO: GameStyle 로직 추가 후 수정 필요
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(targetMember.getId());
        assertThat(response.getProfileImg()).isEqualTo(targetMember.getProfileImage());
        assertThat(response.getMike()).isEqualTo(targetMember.isMike());
        assertThat(response.getGameName()).isEqualTo(targetMember.getGameName());
        assertThat(response.getTag()).isEqualTo(targetMember.getTag());
        assertThat(response.getTier()).isEqualTo(targetMember.getTier());
        assertThat(response.getRank()).isEqualTo(targetMember.getGameRank());
        assertThat(response.getMannerLevel()).isEqualTo(targetMember.getMannerLevel());
        assertThat(response.getMainP()).isEqualTo(targetMember.getMainPosition());
        assertThat(response.getSubP()).isEqualTo(targetMember.getSubPosition());
        assertThat(response.getWantP()).isEqualTo(targetMember.getWantPosition());
        assertThat(response.getIsAgree()).isEqualTo(targetMember.isAgree());
        assertThat(response.getIsBlind()).isEqualTo(targetMember.isBlind());
        assertThat(response.getLoginType()).isEqualTo(String.valueOf(targetMember.getLoginType()));
        assertThat(response.getWinrate()).isEqualTo(targetMember.getWinRate());
        assertThat(response.getChampionResponseList()).isNotNull();

        List<Champion> championList =
                targetMember.getMemberChampionList().stream().map(MemberChampion::getChampion).toList();
        List<Long> championIds = championList.stream().map(Champion::getId).toList();

        for (int i = 0; i < championIds.size(); i++) {
            assertThat(response.getChampionResponseList().get(i).getChampionId()).isEqualTo(championIds.get(i));
        }
    }

    @DisplayName("프로필 이미지 변경 성공")
    @Test
    void setProfileImage() {
        // given
        ProfileImageRequest request = ProfileImageRequest.builder()
                .profileImage(2)
                .build();
        // when
        memberFacadeService.setProfileImage(member, request);
        // then
        assertThat(member.getProfileImage()).isEqualTo(request.getProfileImage());
    }

    @DisplayName("마이크 유무 변경 성공")
    @Test
    void setMike() {
        // given
        IsMikeRequest request = IsMikeRequest.builder()
                .isMike(true)
                .build();
        // when
        memberFacadeService.setIsMike(member, request);
        // then
        assertThat(member.isMike()).isEqualTo(request.getIsMike());
    }

    @DisplayName("주/부/원하는 포지션 변경 성공")
    @Test
    void setPosition() {
        // given
        PositionRequest request = PositionRequest.builder()
                .mainP(1)
                .subP(2)
                .wantP(3)
                .build();
        // when
        memberFacadeService.setPosition(member, request);
        // then
        assertThat(member.getMainPosition()).isEqualTo(request.getMainP());
        assertThat(member.getSubPosition()).isEqualTo(request.getSubP());
        assertThat(member.getWantPosition()).isEqualTo(request.getWantP());
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

    private Champion initChampion(Long id, String name) {
        Champion champion = Champion.create(id, name);
        championRepository.save(champion);
        return champion;
    }

    private void initMemberChampion(Member member, List<Long> top3ChampionIds) {
        top3ChampionIds.forEach(championId -> {
            Champion champion = championRepository.findById(championId)
                    .orElseThrow(() -> new RiotException(ErrorCode.CHAMPION_NOT_FOUND));
            MemberChampion memberChampion = MemberChampion.create(champion, member);
            memberChampionRepository.save(memberChampion);
        });

    }

}
