package com.gamegoo.gamegoo_v2.integration.block;

import com.gamegoo.gamegoo_v2.block.domain.Block;
import com.gamegoo.gamegoo_v2.block.repository.BlockRepository;
import com.gamegoo.gamegoo_v2.block.service.BlockFacadeService;
import com.gamegoo.gamegoo_v2.exception.BlockException;
import com.gamegoo.gamegoo_v2.exception.MemberException;
import com.gamegoo.gamegoo_v2.exception.common.ErrorCode;
import com.gamegoo.gamegoo_v2.member.domain.LoginType;
import com.gamegoo.gamegoo_v2.member.domain.Member;
import com.gamegoo.gamegoo_v2.member.domain.Tier;
import com.gamegoo.gamegoo_v2.member.repository.MemberRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ActiveProfiles("test")
@SpringBootTest
class BlockFacadeServiceTest {

    @Autowired
    private BlockFacadeService blockFacadeService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private BlockRepository blockRepository;

    private static final String MEMBER_EMAIL = "test@gmail.com";
    private static final String MEMBER_GAMENAME = "member";
    private static final String TARGET_EMAIL = "target@naver.com";
    private static final String TARGET_GAMENAME = "target";

    private Member member;

    @BeforeEach
    void setUp() {
        member = createMember(MEMBER_EMAIL, MEMBER_GAMENAME);
    }

    @AfterEach
    void tearDown() {
        blockRepository.deleteAllInBatch();
        memberRepository.deleteAllInBatch();
    }

    @Nested
    @DisplayName("회원 차단")
    class BlockMemberTest {

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
            Member targetMember = createMember(TARGET_EMAIL, TARGET_GAMENAME);

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
            assertTrue(blockRepository.existsByBlockerMemberAndBlockedMember(member, targetMember));

            // 채팅방에서 퇴장 처리 되었는지 검증

            // 친구 관계가 끊어졌는지 검증

            // 친구 요청이 취소되었는지 검증
        }

        @DisplayName("회원 차단 실패: 차단 대상으로 본인 id를 요청한 경우 예외가 발생한다.")
        @Test
        void blockMember_shouldThrowWhenBlockingSelf() {
            // when // then
            assertThatThrownBy(() -> blockFacadeService.blockMember(member, member.getId()))
                    .isInstanceOf(BlockException.class)
                    .hasMessage(ErrorCode.BLOCK_MEMBER_BAD_REQUEST.getMessage());
        }

        @DisplayName("회원 차단 실패: 이미 차단한 회원인 경우 예외가 발생한다.")
        @Test
        void blockMember_shouldThrowWhenAlreadyBlocked() {
            // given
            Member targetMember = createMember(TARGET_EMAIL, TARGET_GAMENAME);

            // 내가 상대를 차단 처리
            blockRepository.save(Block.create(member, targetMember));

            // when // then
            assertThatThrownBy(() -> blockFacadeService.blockMember(member, targetMember.getId()))
                    .isInstanceOf(BlockException.class)
                    .hasMessage(ErrorCode.ALREADY_BLOCKED.getMessage());
        }

        @DisplayName("회원 차단 실패: 차단 대상 회원이 탈퇴한 경우 예외가 발생한다.")
        @Test
        void blockMember_shouldThrowWhenTargetMemberIsBlind() {
            // given
            Member targetMember = createMember(TARGET_EMAIL, TARGET_GAMENAME);

            // 대상 회원을 탈퇴 처리
            targetMember.updateBlind(true);
            memberRepository.save(targetMember);

            // when // then
            assertThatThrownBy(() -> blockFacadeService.blockMember(member, targetMember.getId()))
                    .isInstanceOf(MemberException.class)
                    .hasMessage(ErrorCode.TARGET_MEMBER_DEACTIVATED.getMessage());
        }

        @DisplayName("회원 차단 실패: 차단 대상 회원을 찾을 수 없는 경우 예외가 발생한다.")
        @Test
        void blockMember_shouldThrowWhenTargetMemberNotFound() {
            // when // then
            assertThatThrownBy(() -> blockFacadeService.blockMember(member, 100L))
                    .isInstanceOf(MemberException.class)
                    .hasMessage(ErrorCode.MEMBER_NOT_FOUND.getMessage());
        }

    }

    @Nested
    @DisplayName("회원 차단 해제")
    class UnBlockMemberTest {

        @DisplayName("회원 차단 해제 성공")
        @Test
        void unBlockMemberSucceeds() {
            // given
            Member targetMember = createMember(TARGET_EMAIL, TARGET_GAMENAME);

            // 내가 상대를 차단 처리
            blockRepository.save(Block.create(member, targetMember));

            // when
            blockFacadeService.unBlockMember(member, targetMember.getId());

            // then
            // 차단 기록이 정상적으로 삭제되었는지 검증
            assertFalse(blockRepository.existsByBlockerMemberAndBlockedMember(member, targetMember));
        }

        @DisplayName("회원 차단 해제 실패: 대상 회원이 탈퇴한 경우 예외가 발생한다.")
        @Test
        void unBlockMember_shouldThrowWhenTargetMemberIsBlind() {
            // given
            Member targetMember = createMember(TARGET_EMAIL, TARGET_GAMENAME);

            // 내가 상대를 차단 처리
            blockRepository.save(Block.create(member, targetMember));

            // 대상 회원을 탈퇴 처리
            targetMember.updateBlind(true);
            memberRepository.save(targetMember);

            // when // then
            assertThatThrownBy(() -> blockFacadeService.unBlockMember(member, targetMember.getId()))
                    .isInstanceOf(MemberException.class)
                    .hasMessage(ErrorCode.TARGET_MEMBER_DEACTIVATED.getMessage());
        }

        @DisplayName("회원 차단 해제 실패: 대상 회원을 차단하지 않은 상태인 경우 예외가 발생한다.")
        @Test
        void unBlockMember_shouldThrowWhenTargetMemberIsNotBlocked() {
            // given
            Member targetMember = createMember(TARGET_EMAIL, TARGET_GAMENAME);

            // when // then
            assertThatThrownBy(() -> blockFacadeService.unBlockMember(member, targetMember.getId()))
                    .isInstanceOf(BlockException.class)
                    .hasMessage(ErrorCode.TARGET_MEMBER_NOT_BLOCKED.getMessage());
        }

        @DisplayName("회원 차단 해제 실패: 대상 회원을 찾을 수 없는 경우 예외가 발생한다.")
        @Test
        void unBlockMember_shouldThrowWhenTargetMemberNotFound() {
            // when // then
            assertThatThrownBy(() -> blockFacadeService.unBlockMember(member, 100L))
                    .isInstanceOf(MemberException.class)
                    .hasMessage(ErrorCode.MEMBER_NOT_FOUND.getMessage());
        }

    }

    @Nested
    @DisplayName("차단 목록에서 삭제")
    class DeleteBlockTest {

        @DisplayName("차단 목록에서 삭제 성공")
        @Test
        void deleteBlockSucceeds() {
            // given
            Member targetMember = createMember(TARGET_EMAIL, TARGET_GAMENAME);

            // 내가 상대를 차단 처리
            blockRepository.save(Block.create(member, targetMember));

            // 대상 회원 탈퇴 처리
            targetMember.updateBlind(true);
            memberRepository.save(targetMember);

            // when
            blockFacadeService.deleteBlock(member, targetMember.getId());

            // then
            // 차단 기록 엔티티의 delete 상태가 정상적으로 변경되었는지 검증
            Optional<Block> block = blockRepository.findByBlockerMemberAndBlockedMember(member, targetMember);
            assertTrue(block.get().isDeleted());
        }

        @DisplayName("차단 목록에서 삭제 실패: 대상 회원이 탈퇴하지 않은 경우 예외가 발생한다.")
        @Test
        void deleteBlock_shouldThrowWhenTargetMemberIsNotBlind() {
            // given
            Member targetMember = createMember(TARGET_EMAIL, TARGET_GAMENAME);

            // 회원 차단 처리
            blockFacadeService.blockMember(member, targetMember.getId());

            // when // then
            assertThatThrownBy(() -> blockFacadeService.deleteBlock(member, targetMember.getId()))
                    .isInstanceOf(BlockException.class)
                    .hasMessage(ErrorCode.DELETE_BLOCKED_MEMBER_FAILED.getMessage());
        }

        @DisplayName("차단 목록에서 삭제 실패: 대상 회원을 차단하지 않은 상태인 경우 예외가 발생한다.")
        @Test
        void deleteBlock_shouldThrowWhenTargetMemberIsNotBlocked() {
            // given
            Member targetMember = createMember(TARGET_EMAIL, TARGET_GAMENAME);

            // when // then
            assertThatThrownBy(() -> blockFacadeService.deleteBlock(member, targetMember.getId()))
                    .isInstanceOf(BlockException.class)
                    .hasMessage(ErrorCode.TARGET_MEMBER_NOT_BLOCKED.getMessage());
        }

        @DisplayName("차단 목록에서 삭제 실패: 대상 회원을 찾을 수 없는 경우 예외가 발생한다.")
        @Test
        void deleteBlock_shouldThrowWhenTargetMemberNotFound() {
            // when // then
            assertThatThrownBy(() -> blockFacadeService.deleteBlock(member, 100L))
                    .isInstanceOf(MemberException.class)
                    .hasMessage(ErrorCode.MEMBER_NOT_FOUND.getMessage());
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
