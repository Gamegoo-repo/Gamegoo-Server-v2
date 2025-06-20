package com.gamegoo.gamegoo_v2.core.scheduler;

import com.gamegoo.gamegoo_v2.core.batch.BatchFacadeService;
import com.gamegoo.gamegoo_v2.core.log.DiscordLogger;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Profile({"dev", "prod"})
@Slf4j
@Component
@RequiredArgsConstructor
public class MannerScheduler {

    @Value("${logging.discord.scheduler_webhook}")
    private String webhook;

    private final BatchFacadeService batchFacadeService;
    private final DiscordLogger discordLogger;

    /**
     * 회원 mannerRank 업데이트
     */
    @Scheduled(cron = "0 0 4 * * *")
    public void updateMannerRank() {
        try {
            // mannerScore가 있는 회원 업데이트
            int updated = batchFacadeService.updateMannerRanks();

            // mannerScore가 없는 회원 업데이트
            int reset = batchFacadeService.resetMannerRanks();

            discordLogger.sendTo(
                    String.format("""
                            ✅ [매너 랭크 스케줄러 완료]
                            업데이트 대상: %d명
                            초기화 대상: %d명
                            시간: %s
                            """, updated, reset, LocalDateTime.now()),
                    webhook
            );
        } catch (Exception e) {
            log.error("failed to updateMannerRank Scheduler:", e);
        }
    }

}
