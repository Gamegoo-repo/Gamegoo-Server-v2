package com.gamegoo.gamegoo_v2.social.manner.repository;

import com.gamegoo.gamegoo_v2.social.manner.domain.MannerRatingKeyword;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MannerRatingKeywordRepository extends JpaRepository<MannerRatingKeyword, Long> {

    List<MannerRatingKeyword> findByMannerRatingId(Long id);

}
