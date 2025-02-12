package com.gamegoo.gamegoo_v2.content.board.repository;

import com.gamegoo.gamegoo_v2.account.member.domain.Mike;
import com.gamegoo.gamegoo_v2.account.member.domain.Position;
import com.gamegoo.gamegoo_v2.account.member.domain.Tier;
import com.gamegoo.gamegoo_v2.content.board.domain.Board;
import com.gamegoo.gamegoo_v2.matching.domain.GameMode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface BoardRepository extends JpaRepository<Board, Long> {

    @Query("SELECT b FROM Board b JOIN b.member m WHERE " +
            "b.deleted = false AND " +
            "(:mode IS NULL OR b.gameMode = :mode) AND " +
            "(COALESCE(:soloTier, :freeTier) IS NULL OR m.soloTier = :soloTier OR m.freeTier = :freeTier) AND " +
            "(:mainP IS NULL OR :mainP = 'ANY' OR b.mainP = :mainP) AND " +
            "(:mike IS NULL OR b.mike = :mike)")
    Page<Board> findByFilters(@Param("mode") GameMode gameMode,
                              @Param("soloTier") Tier soloTier,
                              @Param("freeTier") Tier freeTier,
                              @Param("mainP") Position mainP,
                              @Param("mike") Mike mike,
                              Pageable pageable);

    Optional<Board> findByIdAndDeleted(Long boardId, boolean b);


    Page<Board> findByMemberIdAndDeletedFalse(Long memberId, Pageable pageable);

}
