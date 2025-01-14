package com.gamegoo.gamegoo_v2.account.member.repository;

import com.gamegoo.gamegoo_v2.account.member.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

    boolean existsByEmail(String email);

    Optional<Member> findByEmail(String email);

    @Query("SELECT m.id FROM Member m WHERE m.mannerScore IS NOT NULL ORDER BY m.mannerScore desc")
    List<Long> getMemberIdsOrderByMannerScoreIsNotNull();

    @Query("SELECT m.id FROM Member m WHERE m.mannerScore IS NULL AND m.mannerRank IS NOT NULL")
    List<Long> getMemberIdsWhereMannerScoreIsNullAndMannerRankIsNotNull();

}
