package com.gamegoo.gamegoo_v2.social.manner.repository;

import com.gamegoo.gamegoo_v2.social.manner.domain.MannerRating;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MannerRatingRepository extends JpaRepository<MannerRating, Long> {

    boolean existsByFromMemberIdAndToMemberIdAndPositive(Long fromMemberId, Long toMemberId, boolean positive);

}
