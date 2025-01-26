package com.gamegoo.gamegoo_v2.core.scheduler;

import com.gamegoo.gamegoo_v2.core.batch.BatchFacadeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Profile("dev")
@Slf4j
@Component
@RequiredArgsConstructor
public class MannerScheduler {

    private final BatchFacadeService batchFacadeService;

    /**
     * 회원 mannerRank 업데이트
     */
    @Scheduled(cron = "0 0 4 * * *")
    public void updateMannerRank() {
        try {
            // mannerScore가 있는 회원 업데이트
            batchFacadeService.updateMannerRanks();

            // mannerScore가 없는 회원 업데이트
            batchFacadeService.resetMannerRanks();
        } catch (Exception e) {
            log.error("failed to updateMannerRank Scheduler:", e);
        }
    }

}
