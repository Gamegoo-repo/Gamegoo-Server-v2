package com.gamegoo.gamegoo_v2.social.block.dto;

import com.gamegoo.gamegoo_v2.account.member.domain.Member;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
@Builder
public class BlockListResponse {

    List<BlockedMemberResponse> blockedMemberList;
    int listSize;
    int totalPage;
    long totalElements;
    Boolean isFirst;
    Boolean isLast;

    @Getter
    @Builder
    public static class BlockedMemberResponse {

        Long memberId;
        int profileImg;
        String name;
        boolean isBlind;

        public static BlockedMemberResponse of(Member member) {
            String name = member.isBlind() ? "(탈퇴한 사용자)" : member.getGameName();
            return BlockedMemberResponse.builder()
                    .memberId(member.getId())
                    .profileImg(member.getProfileImage())
                    .name(name)
                    .isBlind(member.isBlind())
                    .build();
        }

    }

    public static BlockListResponse of(Page<Member> memberPage) {
        List<BlockedMemberResponse> blockMemberList = memberPage.stream()
                .map(BlockedMemberResponse::of)
                .toList();

        return BlockListResponse.builder()
                .blockedMemberList(blockMemberList)
                .listSize(blockMemberList.size())
                .totalPage(memberPage.getTotalPages())
                .totalElements(memberPage.getTotalElements())
                .isFirst(memberPage.isFirst())
                .isLast(memberPage.isLast())
                .build();
    }

}
