package com.gamegoo.gamegoo_v2.social.block.repository;

import com.gamegoo.gamegoo_v2.account.member.domain.Member;
import com.gamegoo.gamegoo_v2.social.block.domain.Block;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface BlockRepository extends JpaRepository<Block, Long>, BlockRepositoryCustom {

    boolean existsByBlockerMemberAndBlockedMemberAndDeleted(Member blockerMember, Member blockedMember,
                                                            Boolean deleted);

    @Query("""
            SELECT m
            FROM Member m
            INNER JOIN Block b
            ON m.id = b.blockedMember.id
            WHERE b.blockerMember.id = :blockerId
            AND b.deleted = false
            ORDER BY b.createdAt DESC
            """)
    Page<Member> findBlockedMembersByBlockerMember(@Param("blockerId") Long blockerId, Pageable pageable);

    Optional<Block> findByBlockerMemberAndBlockedMemberAndDeleted(Member blockerMember, Member blockedMember,
                                                                  Boolean deleted);

}
