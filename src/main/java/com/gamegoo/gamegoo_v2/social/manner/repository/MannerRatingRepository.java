package com.gamegoo.gamegoo_v2.social.manner.repository;

import com.gamegoo.gamegoo_v2.social.manner.domain.MannerRating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface MannerRatingRepository extends JpaRepository<MannerRating, Long> {

    boolean existsByFromMemberIdAndToMemberIdAndPositive(Long fromMemberId, Long toMemberId, boolean positive);

    Optional<MannerRating> findByFromMemberIdAndToMemberIdAndPositive(Long fromMemberId, Long toMemberId,
                                                                      boolean positive);

    @Query("""
            SELECT COUNT(DISTINCT mr.fromMember.id)
            FROM MannerRating mr
            WHERE mr.toMember.id = :memberId
            AND mr.positive = :positive
            """)
    int countFromMemberByToMemberIdAndPositive(@Param("memberId") Long memberId, @Param("positive") boolean positive);

}
