package com.gamegoo.gamegoo_v2.social.block.dto;

import com.gamegoo.gamegoo_v2.account.member.domain.Member;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
@Builder
public class BlockListResponse {

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    List<BlockedMemberResponse> blockedMemberList;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    int listSize;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    int totalPage;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    long totalElements;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    Boolean isFirst;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    Boolean isLast;

    @Getter
    @Builder
    public static class BlockedMemberResponse {

        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        Long memberId;
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        int profileImg;
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String name;
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        boolean isBlind;

        public static BlockedMemberResponse of(Member member) {
            String name = member.getBlind() ? "(탈퇴한 사용자)" : member.getGameName();
            return BlockedMemberResponse.builder()
                    .memberId(member.getId())
                    .profileImg(member.getProfileImage())
                    .name(name)
                    .isBlind(member.getBlind())
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
