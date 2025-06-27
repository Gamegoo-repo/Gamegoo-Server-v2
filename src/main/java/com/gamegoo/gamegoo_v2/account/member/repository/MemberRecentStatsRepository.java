package com.gamegoo.gamegoo_v2.account.member.repository;

import com.gamegoo.gamegoo_v2.account.member.domain.MemberRecentStats;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRecentStatsRepository extends JpaRepository<MemberRecentStats, Long> {
} 