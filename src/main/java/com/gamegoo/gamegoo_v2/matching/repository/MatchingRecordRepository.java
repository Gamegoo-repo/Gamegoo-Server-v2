package com.gamegoo.gamegoo_v2.matching.repository;

import com.gamegoo.gamegoo_v2.matching.domain.GameMode;
import com.gamegoo.gamegoo_v2.matching.domain.MatchingRecord;
import com.gamegoo.gamegoo_v2.matching.domain.MatchingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface MatchingRecordRepository extends JpaRepository<MatchingRecord, String> {

    @Query(value = """
                SELECT m
                FROM MatchingRecord m
                WHERE m.createdAt > :createdAt
                  AND m.status = :status
                  AND m.gameMode = :gameMode
                GROUP BY m.member.id
            """)
    List<MatchingRecord> findMatchingRecordsWithGroupBy(
            @Param("createdAt") LocalDateTime createdAt,
            @Param("status") MatchingStatus status,
            @Param("gameMode") GameMode gameMode
    );

}
