package com.gamegoo.gamegoo_v2.member.domain;

import com.gamegoo.gamegoo_v2.common.BaseDateTimeEntity;
import com.gamegoo.gamegoo_v2.game.domain.Champion;
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
public class MemberChampion extends BaseDateTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_champion_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "champion_id", nullable = false)
    private Champion champion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    public static MemberChampion create(Champion champion, Member member) {
        MemberChampion memberChampion = MemberChampion.builder()
                .champion(champion)
                .build();
        memberChampion.setMember(member);
        return memberChampion;
    }

    @Builder
    private MemberChampion(Champion champion, Member member) {
        this.champion = champion;
        this.member = member;
    }

    public void setMember(Member member) {
        if (this.member != null) {
            this.member.getMemberChampionList().remove(this);
        }
        this.member = member;
        member.getMemberChampionList().add(this);
    }

}
