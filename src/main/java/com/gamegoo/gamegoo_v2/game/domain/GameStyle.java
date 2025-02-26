package com.gamegoo.gamegoo_v2.game.domain;

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
public class GameStyle extends BaseDateTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "gamestyle_id")
    private Long id;

    @Column(nullable = false, length = 1000)
    private String styleName;

    public static GameStyle create(String styleName) {
        GameStyle gameStyle = GameStyle.builder()
                .styleName(styleName)
                .build();
        return gameStyle;
    }

    @Builder
    private GameStyle(String styleName) {
        this.styleName = styleName;
    }

}
