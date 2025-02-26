package com.gamegoo.gamegoo_v2.social.block.domain;

import com.gamegoo.gamegoo_v2.core.common.BaseDateTimeEntity;
import com.gamegoo.gamegoo_v2.account.member.domain.Member;
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
        return Block.builder()
                .blockerMember(blockerMember)
                .blockedMember(blockedMember)
                .build();
    }

    @Builder
    private Block(boolean deleted, Member blockerMember, Member blockedMember) {
        this.deleted = deleted;
        this.blockerMember = blockerMember;
        this.blockedMember = blockedMember;
    }

    // Block 엔티티의 deleted를 변경하는 메소드
    public void updateDeleted(Boolean deleted) {
        this.deleted = deleted;
    }

}
