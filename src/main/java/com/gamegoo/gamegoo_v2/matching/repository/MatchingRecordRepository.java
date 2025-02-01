package com.gamegoo.gamegoo_v2.matching.repository;

import com.gamegoo.gamegoo_v2.matching.domain.GameMode;
import com.gamegoo.gamegoo_v2.matching.domain.MatchingRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface MatchingRecordRepository extends JpaRepository<MatchingRecord, String>,
        MatchingRecordRepositoryCustom {

    /**
     * 5분 이내, 특정 게임 모드, PENDING 상태의 매칭 레코드 조회
     */
    default List<MatchingRecord> findRecentValidMatchingRecords(GameMode gameMode) {
        return findValidMatchingRecords(LocalDateTime.now().minusMinutes(5), gameMode);
    }

}
