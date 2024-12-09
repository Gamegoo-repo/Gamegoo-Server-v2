package com.gamegoo.gamegoo_v2.block.domain;

import com.gamegoo.gamegoo_v2.common.BaseDateTimeEntity;
import com.gamegoo.gamegoo_v2.member.domain.Member;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Block extends BaseDateTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "block_id")
    private Long id;

    @Column(nullable = false)
    private boolean deleted = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "blocker_id", nullable = false)
    private Member blockerMember;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "blocked_id", nullable = false)
    private Member blockedMember;

    public static Block create(Member blockerMember, Member blockedMember) {
        Block block = Block.builder()
                .blockedMember(blockedMember)
                .build();
        block.setBlockerMember(blockerMember); // 양방향 관계 설정
        return block;
    }

    @Builder
    private Block(boolean deleted, Member blockerMember, Member blockedMember) {
        this.deleted = deleted;
        this.blockerMember = blockerMember;
        this.blockedMember = blockedMember;
    }

    public void setBlockerMember(Member member) {
        if (this.blockerMember != null) {
            this.blockerMember.getBlockList().remove(this);
        }
        this.blockerMember = member;
        member.getBlockList().add(this);
    }

    // Block 엔티티 매핑 해제를 위한 메소드
    public void removeBlockerMember(Member blockerMember) {
        blockerMember.getBlockList().remove(this);
        this.blockerMember = null;
    }

}
