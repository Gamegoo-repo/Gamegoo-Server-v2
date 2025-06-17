package com.gamegoo.gamegoo_v2.content.board.repository;

import com.gamegoo.gamegoo_v2.account.member.domain.Member;
import com.gamegoo.gamegoo_v2.account.member.domain.Mike;
import com.gamegoo.gamegoo_v2.account.member.domain.Position;
import com.gamegoo.gamegoo_v2.account.member.domain.Tier;
import com.gamegoo.gamegoo_v2.content.board.domain.Board;
import com.gamegoo.gamegoo_v2.matching.domain.GameMode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BoardRepository extends JpaRepository<Board, Long> {

    @Query("SELECT b FROM Board b JOIN b.member m WHERE " +
            "b.deleted = false AND " +
            "(:gameMode IS NULL OR b.gameMode = :gameMode) AND " +
            "(:tier IS NULL OR (CASE WHEN b.gameMode = com.gamegoo.gamegoo_v2.matching.domain.GameMode.FREE THEN m.freeTier ELSE m.soloTier END) = :tier) AND " +
            "(:mainPList IS NULL OR b.mainP IN :mainPList) AND " +
            "(:subPList IS NULL OR b.subP IN :subPList) AND " +
            "(:mike IS NULL OR b.mike = :mike) " +
            "ORDER BY GREATEST(COALESCE(b.bumpTime, b.createdAt), b.createdAt) DESC")
    Page<Board> findByGameModeAndTierAndMainPInAndSubPInAndMikeAndDeletedFalse(
            @Param("gameMode") GameMode gameMode,
            @Param("tier") Tier tier,
            @Param("mainPList") List<Position> mainPList,
            @Param("subPList") List<Position> subPList,
            @Param("mike") Mike mike,
            Pageable pageable);

    Optional<Board> findByIdAndDeleted(Long boardId, boolean b);

    Optional<Board> findTopByMemberIdOrderByCreatedAtDesc(Long memberId);

    Page<Board> findByMemberIdAndDeletedFalse(Long memberId, Pageable pageable);
    Slice<Board> findByMemberIdAndDeletedFalseAndIdLessThan(Long memberId, Long cursor, Pageable pageable);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Board b SET b.deleted = true where b.member = :member")
    void deleteAllByMember(@Param("member") Member member);

}
