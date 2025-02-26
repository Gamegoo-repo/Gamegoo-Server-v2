package com.gamegoo.gamegoo_v2.game.domain;

import com.gamegoo.gamegoo_v2.core.common.BaseDateTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Champion extends BaseDateTimeEntity {

    @Id
    @Column(name = "champion_id")
    private Long id;

    @Column(nullable = false, length = 30)
    private String name;

    public static Champion create(Long id, String name) {
        Champion champion = Champion.builder()
                .id(id)
                .name(name)
                .build();
        return champion;
    }

    @Builder
    private Champion(Long id, String name) {
        this.id = id;
        this.name = name;
    }

}
