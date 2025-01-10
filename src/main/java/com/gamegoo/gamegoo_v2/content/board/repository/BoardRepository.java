package com.gamegoo.gamegoo_v2.content.board.repository;

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
            "(:mainPosition IS NULL OR b.mainPosition = :mainPosition ) AND " +
            "(:mike IS NULL OR b.mike = :mike)")
    Page<Board> findByFilters(@Param("mode") Integer mode,
                              @Param("tier") Tier tier,
                              @Param("mainPosition") Integer mainPosition,
                              @Param("mike") Boolean mike,
                              Pageable pageable);


    Optional<Board> findByIdAndDeleted(Long boardId, boolean b);
    

}
