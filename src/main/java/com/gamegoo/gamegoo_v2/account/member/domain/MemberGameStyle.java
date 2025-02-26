package com.gamegoo.gamegoo_v2.account.member.domain;

import com.gamegoo.gamegoo_v2.core.common.BaseDateTimeEntity;
import com.gamegoo.gamegoo_v2.game.domain.GameStyle;
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
public class MemberGameStyle extends BaseDateTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_gamestyle_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gamestyle_id", nullable = false)
    private GameStyle gameStyle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    public static MemberGameStyle create(GameStyle gameStyle, Member member) {
        MemberGameStyle memberGameStyle = MemberGameStyle.builder()
                .gameStyle(gameStyle)
                .build();
        memberGameStyle.setMember(member);
        return memberGameStyle;
    }

    @Builder
    private MemberGameStyle(GameStyle gameStyle, Member member) {
        this.gameStyle = gameStyle;
        this.member = member;
    }

    public void setMember(Member member) {
        if (this.member != null) {
            this.member.getMemberGameStyleList().remove(this);
        }
        this.member = member;
        member.getMemberGameStyleList().add(this);
    }

    // 양방향 관계 제거 메서드
    public void removeMember(Member member) {
        if (this.member != null && this.member.equals(member)) {
            this.member.getMemberGameStyleList().remove(this);
            this.member = null;
        }
    }

}
