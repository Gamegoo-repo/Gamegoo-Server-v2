package com.gamegoo.gamegoo_v2.block.service;

import com.gamegoo.gamegoo_v2.block.domain.Block;
import com.gamegoo.gamegoo_v2.block.dto.BlockListResponse;
import com.gamegoo.gamegoo_v2.block.dto.BlockResponse;
import com.gamegoo.gamegoo_v2.friend.service.FriendService;
import com.gamegoo.gamegoo_v2.member.domain.Member;
import com.gamegoo.gamegoo_v2.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BlockFacadeService {

    private final MemberService memberService;
    private final BlockService blockService;
    private final FriendService friendService;

    /**
     * 회원 차단 Facade 메소드
     *
     * @param member
     * @param targetMemberId
     */
    @Transactional
    public BlockResponse blockMember(Member member, Long targetMemberId) {
        Member targetMember = memberService.findMember(targetMemberId);

        // 회원 차단 처리
        Block block = blockService.blockMember(member, targetMember);

        // 차단 대상 회원과의 채팅방이 존재하는 경우, 해당 채팅방 퇴장 처리

        // 차단 대상 회원과 친구관계인 경우, 친구 관계 끊기
        friendService.removeFriendshipIfPresent(member, targetMember);

        // 차단 대상 회원에게 보냈던 친구 요청이 있는 경우, 해당 요청 취소 처리
        friendService.cancelPendingFriendRequest(member, targetMember);

        return BlockResponse.of(block.getBlockedMember().getId(), "회원 차단 성공");
    }

    /**
     * 차단한 회원 목록 조회 메소드
     *
     * @param member
     * @param pageIdx
     * @return
     */
    public BlockListResponse getBlockList(Member member, Integer pageIdx) {
        Page<Member> members = blockService.getBlockedMemberPage(member.getId(), pageIdx);

        return BlockListResponse.of(members);
    }

    /**
     * 회원 차단 해제 Facade 메소드
     *
     * @param member
     * @param targetMemberId
     */
    @Transactional
    public BlockResponse unBlockMember(Member member, Long targetMemberId) {
        Member targetMember = memberService.findMember(targetMemberId);
        Block block = blockService.unBlockMember(member, targetMember);

        return BlockResponse.of(block.getBlockedMember().getId(), "회원 차단 해제 성공");
    }

    /**
     * targetMember가 탈퇴한 회원인 경우 member의 차단 목록에서 targetMember 삭제 Facade 메소드
     *
     * @param member
     * @param targetMemberId
     */
    @Transactional
    public BlockResponse deleteBlock(Member member, Long targetMemberId) {
        Member targetMember = memberService.findMember(targetMemberId);
        Block block = blockService.deleteBlock(member, targetMember);

        return BlockResponse.of(block.getBlockedMember().getId(), "차단 목록에서 삭제 성공");
    }

}
