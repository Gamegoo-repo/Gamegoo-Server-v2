package com.gamegoo.gamegoo_v2.scheduler;

import com.gamegoo.gamegoo_v2.social.manner.service.MannerFacadeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class MannerScheduler {

    private final MannerFacadeService mannerFacadeService;

    /**
     * 회원 mannerRank 업데이트
     */
    @Transactional
    @Scheduled(cron = "0 0 4 * * *")
    public void updateMannerRank() {
        try {
            // mannerScore가 있는 회원 업데이트
            mannerFacadeService.updateMannerRanks();

            // mannerScore가 없는 회원 업데이트
            mannerFacadeService.resetMannerRanks();
        } catch (Exception e) {
            log.error("failed to updateMannerRank Scheduler:", e);
        }
    }

}
