package com.gamegoo.gamegoo_v2;

import com.gamegoo.gamegoo_v2.notification.domain.NotificationType;
import com.gamegoo.gamegoo_v2.notification.domain.NotificationTypeTitle;
import com.gamegoo.gamegoo_v2.notification.repository.NotificationTypeRepository;
import com.gamegoo.gamegoo_v2.social.manner.domain.MannerKeyword;
import com.gamegoo.gamegoo_v2.social.manner.repository.MannerKeywordRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@SpringBootTest
public class TestSetup {

    @Configuration
    static class TestConfig {

        @Bean
        CommandLineRunner initMannerKeywords(MannerKeywordRepository mannerKeywordRepository) {
            return args -> {
                if (mannerKeywordRepository.count() == 0) {
                    List<MannerKeyword> mannerKeywords = List.of(
                            MannerKeyword.create("캐리했어요", true),
                            MannerKeyword.create("1인분 이상은 해요", true),
                            MannerKeyword.create("욕 안해요", true),
                            MannerKeyword.create("남탓 안해요", true),
                            MannerKeyword.create("매너 있어요", true),
                            MannerKeyword.create("답장 빠름", true),
                            MannerKeyword.create("탈주", false),
                            MannerKeyword.create("욕설", false),
                            MannerKeyword.create("고의 트롤", false),
                            MannerKeyword.create("대리 사용자", false),
                            MannerKeyword.create("소환사명 불일치", false),
                            MannerKeyword.create("답장이 없어요", false)
                    );
                    mannerKeywordRepository.saveAll(mannerKeywords);
                }

            };
        }

        @Bean
        CommandLineRunner initNotificationTypes(NotificationTypeRepository notificationTypeRepository) {
            return args -> {
                if (notificationTypeRepository.count() == 0) {
                    List<NotificationType> notificationTypes = List.of(
                            NotificationType.create(NotificationTypeTitle.FRIEND_REQUEST_SEND),
                            NotificationType.create(NotificationTypeTitle.FRIEND_REQUEST_RECEIVED),
                            NotificationType.create(NotificationTypeTitle.FRIEND_REQUEST_ACCEPTED),
                            NotificationType.create(NotificationTypeTitle.FRIEND_REQUEST_REJECTED),
                            NotificationType.create(NotificationTypeTitle.MANNER_LEVEL_UP),
                            NotificationType.create(NotificationTypeTitle.MANNER_LEVEL_DOWN),
                            NotificationType.create(NotificationTypeTitle.MANNER_KEYWORD_RATED),
                            NotificationType.create(NotificationTypeTitle.REPORT_PROCESSED_REPORTER),
                            NotificationType.create(NotificationTypeTitle.REPORT_PROCESSED_REPORTED)
                    );
                    notificationTypeRepository.saveAll(notificationTypes);
                }

            };
        }

    }

}
