package com.gamegoo.gamegoo_v2.account.member.repository;

import com.gamegoo.gamegoo_v2.account.member.domain.Member;
import com.gamegoo.gamegoo_v2.account.member.domain.MemberChampion;
import com.gamegoo.gamegoo_v2.game.domain.Champion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberChampionRepository extends JpaRepository<MemberChampion, Long> {

    void deleteByMember(Member member);
    
    Optional<MemberChampion> findByMemberAndChampion(Member member, Champion champion);

}
