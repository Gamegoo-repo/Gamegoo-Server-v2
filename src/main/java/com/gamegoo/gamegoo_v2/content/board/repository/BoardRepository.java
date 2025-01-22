package com.gamegoo.gamegoo_v2.content.board.repository;

import com.gamegoo.gamegoo_v2.account.member.domain.Mike;
import com.gamegoo.gamegoo_v2.account.member.domain.Position;
import com.gamegoo.gamegoo_v2.account.member.domain.Tier;
import com.gamegoo.gamegoo_v2.content.board.domain.Board;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface BoardRepository extends JpaRepository<Board, Long> {

    @Query("SELECT b From Board b JOIN b.member m WHERE" +
            "(b.deleted = false) AND " +
            "(:mode IS NULL OR b.mode = :mode) AND " +
            "(:tier IS NULL OR m.tier = :tier) AND " +
            "(:mainPosition IS NULL OR :mainPosition = 'ANY' OR b.mainPosition = :mainPosition ) AND " +
            "(:mike IS NULL OR b.mike = :mike)")
    Page<Board> findByFilters(@Param("mode") Integer mode,
                              @Param("tier") Tier tier,
                              @Param("mainPosition") Position mainPosition,
                              @Param("mike") Mike mike,
                              Pageable pageable);


    Optional<Board> findByIdAndDeleted(Long boardId, boolean b);


    Page<Board> findByMemberIdAndDeletedFalse(Long memberId, Pageable pageable);

}
