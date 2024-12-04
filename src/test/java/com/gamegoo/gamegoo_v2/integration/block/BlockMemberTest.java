package com.gamegoo.gamegoo_v2.integration.block;

import com.gamegoo.gamegoo_v2.block.repository.BlockRepository;
import com.gamegoo.gamegoo_v2.block.service.BlockFacadeService;
import com.gamegoo.gamegoo_v2.exception.BlockException;
import com.gamegoo.gamegoo_v2.exception.common.MemberException;
import com.gamegoo.gamegoo_v2.member.domain.LoginType;
import com.gamegoo.gamegoo_v2.member.domain.Member;
import com.gamegoo.gamegoo_v2.member.domain.Tier;
import com.gamegoo.gamegoo_v2.member.repository.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
public class BlockMemberTest {

    @Autowired
    private BlockFacadeService blockFacadeService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private BlockRepository blockRepository;

    @DisplayName("회원 차단 성공")
    @ParameterizedTest(name = "채팅방: {0}, 친구 관계: {1}, 친구 요청: {2}")
    //@CsvSource({
    //        "true, true, true",  // 채팅방 있음, 친구 관계 있음, 친구 요청 있음
    //        "true, true, false", // 채팅방 있음, 친구 관계 있음, 친구 요청 없음
    //        "true, false, true", // 채팅방 있음, 친구 관계 없음, 친구 요청 있음
    //        "true, false, false",// 채팅방 있음, 친구 관계 없음, 친구 요청 없음
    //        "false, true, true", // 채팅방 없음, 친구 관계 있음, 친구 요청 있음
    //        "false, true, false",// 채팅방 없음, 친구 관계 있음, 친구 요청 없음
    //        "false, false, true",// 채팅방 없음, 친구 관계 없음, 친구 요청 있음
    //        "false, false, false"// 채팅방 없음, 친구 관계 없음, 친구 요청 없음
    //})
    @CsvSource({
            "false, false, false"
    })
    void blockMemberSucceeds(boolean chatroomExists, boolean friendshipExists, boolean friendRequestExists) {
        // given
        Member member = createMyMember();
        Member targetMember = createTargetMember();

        // 조건에 따른 상태 설정
        //if (chatroomExists) {
        //    //chatQueryService.createChatroom(member, targetMember);
        //}
        //
        //if (friendshipExists) {
        //    //friendService.addFriend(member, targetMember);
        //}
        //
        //if (friendRequestExists) {
        //    //friendService.sendFriendRequest(member, targetMember);
        //}

        // when
        blockFacadeService.blockMember(member, targetMember.getId());

        // then
        // 차단이 정상적으로 처리되었는지 검증
        assertThat(blockRepository.existsByBlockerMemberAndBlockedMember(member, targetMember)).isTrue();

        // 채팅방에서 퇴장 처리 되었는지 검증

        // 친구 관계가 끊어졌는지 검증

        // 친구 요청이 취소되었는지 검증
    }

    @DisplayName("회원 차단 실패: 차단 대상으로 본인 id를 요청한 경우 예외가 발생한다.")
    @Test
    void blockMember_shouldThrowWhenBlockingSelf() {
        // given
        Member member = createMyMember();

        // when // then
        assertThatThrownBy(() -> blockFacadeService.blockMember(member, member.getId()))
                .isInstanceOf(BlockException.class)
                .hasMessage("잘못된 친구 차단 요청입니다.");
    }

    @DisplayName("회원 차단 실패: 이미 차단한 회원인 경우 예외가 발생한다.")
    @Test
    void blockMember_shouldThrowWhenAlreadyBlocked() {
        // given
        Member member = createMyMember();
        Member targetMember = createTargetMember();

        // 차단 처리
        blockFacadeService.blockMember(member, targetMember.getId());

        // when // then
        assertThatThrownBy(() -> blockFacadeService.blockMember(member, targetMember.getId()))
                .isInstanceOf(BlockException.class)
                .hasMessage("이미 차단한 회원입니다.");
    }

    @DisplayName("회원 차단 실패: 차단 대상 회원이 탈퇴한 경우 예외가 발생한다.")
    @Test
    void blockMember_shouldThrowWhenTargetMemberIsBlind() {
        // given
        Member member = createMyMember();
        Member targetMember = createTargetMember();

        // 대상 회원을 탈퇴 처리
        targetMember.updateBlind(true);

        // when // then
        assertThatThrownBy(() -> blockFacadeService.blockMember(member, targetMember.getId()))
                .isInstanceOf(MemberException.class)
                .hasMessage("대상 회원이 탈퇴했습니다.");
    }

    private Member createMyMember() {
        return memberRepository.save(Member.builder()
                .email("test1@naver.com")
                .password("test1password")
                .profileImage(1)
                .loginType(LoginType.GENERAL)
                .gameName("test1")
                .tag("tag1")
                .tier(Tier.IRON)
                .gameRank(0)
                .winRate(0.0)
                .gameCount(0)
                .isAgree(true)
                .build());
    }

    private Member createTargetMember() {
        return memberRepository.save(Member.builder()
                .email("test2@naver.com")
                .password("test2password")
                .profileImage(2)
                .loginType(LoginType.GENERAL)
                .gameName("test2")
                .tag("tag2")
                .tier(Tier.IRON)
                .gameRank(0)
                .winRate(0.0)
                .gameCount(0)
                .isAgree(true)
                .build());
    }

}
