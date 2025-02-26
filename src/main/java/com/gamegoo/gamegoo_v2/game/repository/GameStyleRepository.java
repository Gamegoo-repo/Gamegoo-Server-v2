package com.gamegoo.gamegoo_v2.game.repository;

import com.gamegoo.gamegoo_v2.game.domain.GameStyle;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GameStyleRepository extends JpaRepository<GameStyle, Long> {

    Optional<GameStyle> findById(Long gameStyleId);

}
