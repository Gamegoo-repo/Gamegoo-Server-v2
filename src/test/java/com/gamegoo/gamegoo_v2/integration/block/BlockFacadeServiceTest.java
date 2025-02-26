package com.gamegoo.gamegoo_v2.integration.block;

import com.gamegoo.gamegoo_v2.account.member.domain.LoginType;
import com.gamegoo.gamegoo_v2.account.member.domain.Member;
import com.gamegoo.gamegoo_v2.account.member.domain.Tier;
import com.gamegoo.gamegoo_v2.account.member.repository.MemberRepository;
import com.gamegoo.gamegoo_v2.core.exception.BlockException;
import com.gamegoo.gamegoo_v2.core.exception.MemberException;
import com.gamegoo.gamegoo_v2.core.exception.common.ErrorCode;
import com.gamegoo.gamegoo_v2.social.block.domain.Block;
import com.gamegoo.gamegoo_v2.social.block.dto.BlockListResponse;
import com.gamegoo.gamegoo_v2.social.block.repository.BlockRepository;
import com.gamegoo.gamegoo_v2.social.block.service.BlockFacadeService;
import com.gamegoo.gamegoo_v2.social.friend.domain.Friend;
import com.gamegoo.gamegoo_v2.social.friend.domain.FriendRequest;
import com.gamegoo.gamegoo_v2.social.friend.domain.FriendRequestStatus;
import com.gamegoo.gamegoo_v2.social.friend.repository.FriendRepository;
import com.gamegoo.gamegoo_v2.social.friend.repository.FriendRequestRepository;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ActiveProfiles("test")
@SpringBootTest
class BlockFacadeServiceTest {

    @Autowired
    private BlockFacadeService blockFacadeService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private BlockRepository blockRepository;

    @Autowired
    private FriendRepository friendRepository;

    @Autowired
    private FriendRequestRepository friendRequestRepository;

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
        friendRepository.deleteAllInBatch();
        friendRequestRepository.deleteAllInBatch();
        memberRepository.deleteAllInBatch();
    }

    @Nested
    @DisplayName("회원 차단")
    class BlockMemberTest {

        @DisplayName("회원 차단 성공")
        @ParameterizedTest(name = "채팅방: {0}, 친구 관계: {1}, 친구 요청: {2}")
        @CsvSource({
                //"true, true, true",  // 채팅방 있음, 친구 관계 있음, 친구 요청 있음
                //"true, true, false", // 채팅방 있음, 친구 관계 있음, 친구 요청 없음
                //"true, false, true", // 채팅방 있음, 친구 관계 없음, 친구 요청 있음
                //"true, false, false",// 채팅방 있음, 친구 관계 없음, 친구 요청 없음
                "false, true, true", // 채팅방 없음, 친구 관계 있음, 친구 요청 있음
                "false, true, false",// 채팅방 없음, 친구 관계 있음, 친구 요청 없음
                "false, false, true",// 채팅방 없음, 친구 관계 없음, 친구 요청 있음
                "false, false, false"// 채팅방 없음, 친구 관계 없음, 친구 요청 없음
        })
        void blockMemberSucceeds(boolean chatroomExists, boolean friendshipExists, boolean friendRequestExists) {
            // given
            Member targetMember = createMember(TARGET_EMAIL, TARGET_GAMENAME);

            // 조건에 따른 상태 설정
            //if (chatroomExists) {
            //    //chatQueryService.createChatroom(member, targetMember);
            //}

            if (friendshipExists) {
                friendRepository.save(Friend.create(member, targetMember));
                friendRepository.save(Friend.create(targetMember, member));
            }

            if (friendRequestExists) {
                friendRequestRepository.save(FriendRequest.create(member, targetMember));
            }

            // when
            blockFacadeService.blockMember(member, targetMember.getId());

            // then
            // 차단이 정상적으로 처리되었는지 검증
            assertThat(blockRepository.existsByBlockerMemberAndBlockedMemberAndDeleted(member, targetMember,
                    false)).isTrue();

            // 채팅방에서 퇴장 처리 되었는지 검증

            // 친구 관계가 끊어졌는지 검증
            if (friendshipExists) {
                boolean exists = friendRepository.existsByFromMemberAndToMember(member, targetMember);
                assertThat(exists).isFalse();
            }

            // 친구 요청이 취소되었는지 검증
            if (friendRequestExists) {
                boolean exists = friendRequestRepository.existsByFromMemberAndToMemberAndStatus(member, targetMember,
                        FriendRequestStatus.CANCELLED);
                assertThat(exists).isTrue();
            }
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
            // 차단 기록 엔티티의 delete 상태가 정상적으로 변경되었는지 검증
            Block block = blockRepository.findByBlockerMemberAndBlockedMember(member, targetMember).get();
            assertThat(block.isDeleted()).isTrue();
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
            Block block = blockRepository.findByBlockerMemberAndBlockedMember(member, targetMember).get();
            assertThat(block.isDeleted()).isTrue();
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

    @Nested
    @DisplayName("차단한 회원 목록 조회")
    class GetBlockListTest {

        @DisplayName("차단한 회원 목록 조회 성공: 차단한 회원이 존재하는 경우")
        @Test
        void getBlockListSucceedsWhenBlockedMemberExists() {
            // given
            for (int i = 1; i <= 5; i++) {
                Member targetMember = createMember("member" + i + "@gmail.com", "member" + i);

                // 내가 상대를 차단 처리
                blockRepository.save(Block.create(member, targetMember));
            }

            // when
            BlockListResponse blockList = blockFacadeService.getBlockList(member, 1);

            // then
            assertThat(blockList.getBlockedMemberList()).hasSize(5);
            assertThat(blockList.getListSize()).isEqualTo(5);
            assertThat(blockList.getTotalPage()).isEqualTo(1);
            assertThat(blockList.getTotalElements()).isEqualTo(5);
            assertThat(blockList.getIsFirst()).isTrue();
            assertThat(blockList.getIsLast()).isTrue();
        }

        @DisplayName("차단한 회원 목록 조회 성공: 차단한 회원이 존재하지 않는 경우")
        @Test
        void getBlockListSucceedsWhenBlockedMemberNotExists() {
            // when
            BlockListResponse blockList = blockFacadeService.getBlockList(member, 1);

            // then
            assertThat(blockList.getBlockedMemberList()).isEmpty();
            assertThat(blockList.getListSize()).isEqualTo(0);
            assertThat(blockList.getTotalPage()).isEqualTo(0);
            assertThat(blockList.getTotalElements()).isEqualTo(0);
            assertThat(blockList.getIsFirst()).isTrue();
            assertThat(blockList.getIsLast()).isTrue();
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
