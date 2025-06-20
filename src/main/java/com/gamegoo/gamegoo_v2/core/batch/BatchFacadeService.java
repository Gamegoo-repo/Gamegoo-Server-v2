package com.gamegoo.gamegoo_v2.core.batch;

import com.gamegoo.gamegoo_v2.social.manner.service.MannerService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class BatchFacadeService {

    private final BatchService batchService;
    private final MannerService mannerService;

    @Value("${batch_size.manner_rank}")
    private int BATCH_SIZE;

    /**
     * mannerScore가 null인 모든 회원의 mannerRank를 null로 업데이트하는 facade 메소드
     */
    public int resetMannerRanks() {
        List<Long> memberIds = mannerService.getMannerRankResetTargets();

        // mannerRank를 null로 설정하는 Map 생성
        Map<Long, Double> mannerRankMap = new HashMap<>();
        memberIds.forEach(memberId -> mannerRankMap.put(memberId, null));

        // 전체 데이터 list 생성
        List<Map.Entry<Long, Double>> entries = new ArrayList<>(mannerRankMap.entrySet());

        int totalUpdated = 0;

        // batch로 나누어 업데이트 처리
        for (int i = 0; i < entries.size(); i += BATCH_SIZE) {
            int end = Math.min(i + BATCH_SIZE, entries.size());
            List<Map.Entry<Long, Double>> batch = entries.subList(i, end);

            totalUpdated += batchService.batchUpdateMannerRanks(batch);
        }

        return totalUpdated;
    }

    /**
     * mannerScore가 null이 아닌 모든 회원의 mannerRank를 계산해 업데이트하는 facade 메소드
     */
    public int updateMannerRanks() {
        List<Long> memberIds = mannerService.getMannerRankUpdateTargets();

        int totalMembers = memberIds.size();

        // mannerRank를 계산해 Map 생성
        Map<Long, Double> mannerRankMap = IntStream.range(0, totalMembers)
                .boxed()
                .collect(Collectors.toMap(
                        memberIds::get,
                        i -> Math.round((double) (i + 1) / totalMembers * 1000) / 10.0
                ));

        // 전체 데이터 list 생성
        List<Map.Entry<Long, Double>> entries = new ArrayList<>(mannerRankMap.entrySet());

        int totalUpdated = 0;

        // batch로 나누어 업데이트 처리
        for (int i = 0; i < entries.size(); i += BATCH_SIZE) {
            int end = Math.min(i + BATCH_SIZE, entries.size());
            List<Map.Entry<Long, Double>> batch = entries.subList(i, end);

            totalUpdated += batchService.batchUpdateMannerRanks(batch);
        }

        return totalUpdated;
    }

}
