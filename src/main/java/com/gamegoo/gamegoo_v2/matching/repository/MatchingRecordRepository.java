package com.gamegoo.gamegoo_v2.matching.repository;

import com.gamegoo.gamegoo_v2.matching.domain.MatchingRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface MatchingRecordRepository extends JpaRepository<MatchingRecord, String>,
        MatchingRecordRepositoryCustom {

    Optional<MatchingRecord> findMatchingRecordsByMatchingUuid(String matchingUuid);

}
