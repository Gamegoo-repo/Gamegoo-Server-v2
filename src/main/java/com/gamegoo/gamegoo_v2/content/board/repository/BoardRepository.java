package com.gamegoo.gamegoo_v2.content.board.repository;

import com.gamegoo.gamegoo_v2.account.member.domain.Member;
import com.gamegoo.gamegoo_v2.account.member.domain.Mike;
import com.gamegoo.gamegoo_v2.account.member.domain.Position;
import com.gamegoo.gamegoo_v2.account.member.domain.Tier;
import com.gamegoo.gamegoo_v2.content.board.domain.Board;
import com.gamegoo.gamegoo_v2.matching.domain.GameMode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BoardRepository extends JpaRepository<Board, Long> {

    @Query("SELECT b FROM Board b WHERE " +
            "b.gameMode = :gameMode AND " +
            "b.member.soloTier = :tier AND " +
            "b.mainP IN :mainPList AND " +
            "b.subP IN :subPList AND " +
            "b.mike = :mike AND " +
            "b.deleted = false " +
            "ORDER BY b.createdAt DESC")
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

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Board b SET b.deleted = true where b.member = :member")
    void deleteAllByMember(@Param("member") Member member);

}
