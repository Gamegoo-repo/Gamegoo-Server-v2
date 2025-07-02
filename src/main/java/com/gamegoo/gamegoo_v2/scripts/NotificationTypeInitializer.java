package com.gamegoo.gamegoo_v2.scripts;

import com.gamegoo.gamegoo_v2.notification.domain.NotificationType;
import com.gamegoo.gamegoo_v2.notification.domain.NotificationTypeTitle;
import com.gamegoo.gamegoo_v2.notification.repository.NotificationTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class NotificationTypeInitializer implements ApplicationListener<ApplicationReadyEvent> {

    private final NotificationTypeRepository notificationTypeRepository;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        if (isCreateMode(event)) {
            try {
                initializeNotificationType();
            } catch (IOException e) {
                System.out.println(e.getClass());
            }
        }
    }

    private boolean isCreateMode(ApplicationReadyEvent event) {
        // jpa.hibernate.ddl-auto 값이 create인지 확인
        String ddlAuto = event.getApplicationContext().getEnvironment().getProperty("spring.jpa.hibernate.ddl-auto");
        return "create".equalsIgnoreCase(ddlAuto);
    }

    private void initializeNotificationType() throws IOException {
        List<NotificationType> notificationTypes = new ArrayList<>();

        notificationTypes.add(NotificationType.create(NotificationTypeTitle.FRIEND_REQUEST_SEND));
        notificationTypes.add(NotificationType.create(NotificationTypeTitle.FRIEND_REQUEST_RECEIVED));
        notificationTypes.add(NotificationType.create(NotificationTypeTitle.FRIEND_REQUEST_ACCEPTED));
        notificationTypes.add(NotificationType.create(NotificationTypeTitle.FRIEND_REQUEST_REJECTED));
        notificationTypes.add(NotificationType.create(NotificationTypeTitle.MANNER_LEVEL_UP));
        notificationTypes.add(NotificationType.create(NotificationTypeTitle.MANNER_LEVEL_DOWN));
        notificationTypes.add(NotificationType.create(NotificationTypeTitle.MANNER_KEYWORD_RATED));

        notificationTypeRepository.saveAll(notificationTypes);
    }

}
