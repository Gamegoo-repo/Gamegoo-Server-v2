package com.gamegoo.gamegoo_v2.social.manner.domain;

import com.gamegoo.gamegoo_v2.account.member.domain.Member;
import com.gamegoo.gamegoo_v2.core.common.BaseDateTimeEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MannerRating extends BaseDateTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "manner_rating_id")
    private Long id;

    @Column(nullable = false)
    private boolean positive;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_member_id", nullable = false)
    private Member fromMember;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_member_id", nullable = false)
    private Member toMember;

    @OneToMany(mappedBy = "mannerRating", cascade = CascadeType.ALL)
    private List<MannerRatingKeyword> mannerRatingKeywordList = new ArrayList<>();

    public static MannerRating create(Member fromMember, Member toMember, boolean positive) {
        return MannerRating.builder()
                .fromMember(fromMember)
                .toMember(toMember)
                .positive(positive)
                .build();
    }

    @Builder
    private MannerRating(boolean positive, Member fromMember, Member toMember) {
        this.positive = positive;
        this.fromMember = fromMember;
        this.toMember = toMember;
    }

}
