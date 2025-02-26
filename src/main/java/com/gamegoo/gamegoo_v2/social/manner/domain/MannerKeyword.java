package com.gamegoo.gamegoo_v2.social.manner.domain;

import com.gamegoo.gamegoo_v2.core.common.BaseDateTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MannerKeyword extends BaseDateTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "manner_keyword_id")
    private Long id;

    @Column(nullable = false, length = 200)
    private String contents;

    @Column(nullable = false)
    private boolean positive;

    public static MannerKeyword create(String contents, boolean positive) {
        return MannerKeyword.builder()
                .contents(contents)
                .positive(positive)
                .build();
    }

    @Builder
    private MannerKeyword(String contents, boolean positive) {
        this.contents = contents;
        this.positive = positive;
    }

}
