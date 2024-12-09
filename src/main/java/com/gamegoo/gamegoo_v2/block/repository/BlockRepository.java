package com.gamegoo.gamegoo_v2.block.repository;

import com.gamegoo.gamegoo_v2.block.domain.Block;
import com.gamegoo.gamegoo_v2.member.domain.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface BlockRepository extends JpaRepository<Block, Long> {

    boolean existsByBlockerMemberAndBlockedMember(Member blockerMember, Member blockedMember);

    @Query("SELECT m FROM Member m INNER JOIN Block b ON m.id = b.blockedMember.id WHERE b.blockerMember.id = " +
            ":blockerId AND b.deleted = false ORDER BY b.createdAt DESC")
    Page<Member> findBlockedMembersByBlockerIdAndNotDeleted(@Param("blockerId") Long blockerId, Pageable pageable);

    Optional<Block> findByBlockerMemberAndBlockedMember(Member blockerMember, Member blockedMember);

}
