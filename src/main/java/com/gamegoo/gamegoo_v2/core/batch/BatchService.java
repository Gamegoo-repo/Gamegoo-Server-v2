package com.gamegoo.gamegoo_v2.core.batch;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BatchService {

    private final EntityManager entityManager;

    /**
     * 배치별 mannerRank 업데이트
     *
     * @param batch 배치로 처리할 데이터
     */
    @Transactional
    public int batchUpdateMannerRanks(List<Map.Entry<Long, Double>> batch) {
        if (batch == null || batch.isEmpty()) {
            return 0;
        }

        String query = buildMannerRankBatchUpdateQuery(batch);
        int executed = entityManager.createNativeQuery(query).executeUpdate();
        entityManager.flush();
        entityManager.clear();

        return executed;
    }

    /**
     * mannerRank batch update 쿼리 생성
     */
    private String buildMannerRankBatchUpdateQuery(List<Map.Entry<Long, Double>> batch) {
        String cases = batch.stream()
                .map(entry -> String.format("WHEN member_id = %d THEN %s", entry.getKey(),
                        entry.getValue() == null ? "NULL" : entry.getValue()))
                .collect(Collectors.joining(" "));

        String ids = batch.stream()
                .map(entry -> String.valueOf(entry.getKey()))
                .collect(Collectors.joining(", "));

        return String.format("UPDATE member SET manner_rank = CASE %s END WHERE member_id IN (%s)", cases, ids);
    }

}
