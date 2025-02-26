package com.gamegoo.gamegoo_v2.game.repository;

import com.gamegoo.gamegoo_v2.game.domain.Champion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ChampionRepository extends JpaRepository<Champion, Long> {

    Optional<Champion> findById(Long id);

}
