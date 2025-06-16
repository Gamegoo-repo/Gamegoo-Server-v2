package com.gamegoo.gamegoo_v2.account.email.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailAsyncService {

    private final EmailService emailService;

    @Async
    public void sendEmailAsync(String to, String subject, String templatePath, Map<String, String> placeholders) {
        try {
            emailService.sendEmail(to, subject, templatePath, placeholders);
        } catch (Exception e) {
            log.error("비동기 이메일 전송 실패", e);
        }
    }

}
