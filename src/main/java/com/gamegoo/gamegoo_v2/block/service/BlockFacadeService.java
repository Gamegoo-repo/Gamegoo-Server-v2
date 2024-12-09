package com.gamegoo.gamegoo_v2.block.service;

import com.gamegoo.gamegoo_v2.block.dto.BlockListResponse;
import com.gamegoo.gamegoo_v2.member.domain.Member;
import com.gamegoo.gamegoo_v2.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BlockFacadeService {

    private final MemberService memberService;
    private final BlockService blockService;

    private final static int PAGE_SIZE = 10;

    /**
     * 회원 차단 Facade 메소드
     *
     * @param member
     * @param targetMemberId
     */
    @Transactional
    public void blockMember(Member member, Long targetMemberId) {
        Member targetMember = memberService.findMember(targetMemberId);
        blockService.blockMember(member, targetMember);
    }

    /**
     * 차단한 회원 목록 조회 메소드
     *
     * @param member
     * @param pageIdx
     * @return
     */
    public BlockListResponse getBlockList(Member member, Integer pageIdx) {
        PageRequest pageRequest = PageRequest.of(pageIdx - 1, PAGE_SIZE);
        Page<Member> members = blockService.findBlockedMembersByBlockerId(member.getId(), pageRequest);

        return BlockListResponse.of(members);
    }

    /**
     * 회원 차단 해제 Facade 메소드
     *
     * @param member
     * @param targetMemberId
     */
    @Transactional
    public void unBlockMember(Member member, Long targetMemberId) {
        Member targetMember = memberService.findMember(targetMemberId);
        blockService.unBlockMember(member, targetMember);
    }

}
