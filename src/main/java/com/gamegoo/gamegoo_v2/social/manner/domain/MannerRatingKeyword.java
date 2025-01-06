package com.gamegoo.gamegoo_v2.social.manner.domain;

import com.gamegoo.gamegoo_v2.core.common.BaseDateTimeEntity;
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
public class MannerRatingKeyword extends BaseDateTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "manner_rating_keyword_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manner_rating_id", nullable = false)
    private MannerRating mannerRating;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manner_keyword_id", nullable = false)
    private MannerKeyword mannerKeyword;

    public static MannerRatingKeyword create(MannerRating mannerRating, MannerKeyword mannerKeyword) {
        MannerRatingKeyword mannerRatingKeyword = MannerRatingKeyword.builder()
                .mannerKeyword(mannerKeyword)
                .build();
        mannerRatingKeyword.setMannerRating(mannerRating); // 양방향 관계 설정
        return mannerRatingKeyword;
    }

    @Builder
    private MannerRatingKeyword(MannerKeyword mannerKeyword) {
        this.mannerKeyword = mannerKeyword;
    }

    public void setMannerRating(MannerRating mannerRating) {
        if (this.mannerRating != null) {
            this.mannerRating.getMannerRatingKeywordList().remove(this);
        }
        this.mannerRating = mannerRating;
        this.mannerRating.getMannerRatingKeywordList().add(this);
    }

}
