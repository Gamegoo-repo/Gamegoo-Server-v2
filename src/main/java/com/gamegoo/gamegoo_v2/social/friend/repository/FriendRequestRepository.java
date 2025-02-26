package com.gamegoo.gamegoo_v2.social.friend.repository;

import com.gamegoo.gamegoo_v2.account.member.domain.Member;
import com.gamegoo.gamegoo_v2.social.friend.domain.FriendRequest;
import com.gamegoo.gamegoo_v2.social.friend.domain.FriendRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FriendRequestRepository extends JpaRepository<FriendRequest, Long> {

    boolean existsByFromMemberAndToMemberAndStatus(Member fromMember, Member toMember, FriendRequestStatus status);

    Optional<FriendRequest> findByFromMemberAndToMemberAndStatus(Member fromMember, Member toMember,
                                                                 FriendRequestStatus status);

    @Query("""
            SELECT fr
            FROM FriendRequest fr
            WHERE fr.status = :status
            AND (
                    (fr.fromMember = :member AND fr.toMember = :targetMember)
                 OR (fr.toMember = :member AND fr.fromMember = :targetMember)
              )
            """)
    Optional<FriendRequest> findBetweenTargetMemberAndStatus(
            @Param("member") Member member,
            @Param("targetMember") Member targetMember,
            @Param("status") FriendRequestStatus status
    );

    @Query("""
            SELECT fr
            FROM FriendRequest fr
            WHERE fr.status = :status
            AND (
                    (fr.fromMember = :member AND fr.toMember.id IN :targetMemberIds)
                 OR (fr.toMember = :member AND fr.fromMember.id IN :targetMemberIds)
              )
            """)
    List<FriendRequest> findAllBetweenTargetMembersAndStatus(
            @Param("member") Member member,
            @Param("targetMemberIds") List<Long> targetMemberIds,
            @Param("status") FriendRequestStatus status
    );

}
