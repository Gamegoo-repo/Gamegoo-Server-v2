package com.gamegoo.gamegoo_v2.core.scheduler;

import com.gamegoo.gamegoo_v2.core.scheduler.handler.MannerMessageHandler;
import com.gamegoo.gamegoo_v2.matching.domain.MannerMessageStatus;
import com.gamegoo.gamegoo_v2.matching.domain.MatchingRecord;
import com.gamegoo.gamegoo_v2.matching.repository.MatchingRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Profile({"prod", "dev", "qa"})
@Slf4j
@Component
@RequiredArgsConstructor
public class SystemMessageScheduler {

    private final MatchingRecordRepository matchingRecordRepository;
    private final MannerMessageHandler mannerMessageHandler;

    private static final Long MANNER_MESSAGE_TIME = 2 * 60L; // 2 * 60초

    @Scheduled(fixedRate = 60 * 1000) // 60초 주기로 실행
    public void mannerSystemMessageRun() {
        LocalDateTime updatedTime = LocalDateTime.now().minusSeconds(MANNER_MESSAGE_TIME);
        List<MatchingRecord> list = matchingRecordRepository
                .findByMannerMessageSentAndUpdatedAtBefore(MannerMessageStatus.NOT_SENT, updatedTime);
        //log.info("-- mannerSystemMessageRun --");
        //log.info(list.toString());
        //log.info("list size: {}", list.size());

        for (MatchingRecord record : list) {
            try {
                mannerMessageHandler.process(record);
            } catch (Exception e) {
                log.error("매너 메시지 처리 실패 - matchingRecord UUID={}", record.getMatchingUuid(), e);
            }
        }
    }

}
