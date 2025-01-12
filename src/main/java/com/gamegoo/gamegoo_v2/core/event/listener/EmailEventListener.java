package com.gamegoo.gamegoo_v2.core.event.listener;

import com.gamegoo.gamegoo_v2.account.email.service.EmailService;
import com.gamegoo.gamegoo_v2.content.report.domain.Report;
import com.gamegoo.gamegoo_v2.content.report.service.ReportService;
import com.gamegoo.gamegoo_v2.core.event.SendReportEmailEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmailEventListener {

    private final EmailService emailService;
    private final ReportService reportService;

    @Value("${email.report_email_to}")
    private String reportEmailTo;

    @Value("${email.report_email_template_path}")
    private String reportTemplatePath;

    private static final String REPORT_EMAIL_SUBJECT = "%s%s 님이 신고를 접수했습니다.";

    @Async
    @EventListener
    public void handleSendReportEmailEvent(SendReportEmailEvent event) {
        try {
            Report report = reportService.findById(event.getReportId());

            String subject = String.format(REPORT_EMAIL_SUBJECT, event.getFromMemberGameName(),
                    event.getFromMemberTag());

            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("TITLE", subject);
            placeholders.put("REPORT_ID", report.getId().toString());
            placeholders.put("REPORT_FROM_MEMBER_ID", event.getFromMemberId().toString());
            placeholders.put("REPORT_TO_MEMBER_ID", event.getToMemberId().toString());
            placeholders.put("REPORT_PATH", report.getPath().name());
            placeholders.put("REPORT_TYPE", reportService.getReportTypeString(event.getReportId()));
            placeholders.put("REPORT_CONTENT", report.getContent());

            emailService.sendEmail(reportEmailTo, subject, reportTemplatePath, placeholders);

        } catch (Exception e) {
            log.error("Failed to send report email", e);
        }
    }

}
