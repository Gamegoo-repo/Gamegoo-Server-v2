package com.gamegoo.gamegoo_v2.account.email.service;

import com.gamegoo.gamegoo_v2.account.email.domain.EmailVerifyRecord;
import com.gamegoo.gamegoo_v2.account.email.repository.EmailVerifyRecordRepository;
import com.gamegoo.gamegoo_v2.core.exception.EmailException;
import com.gamegoo.gamegoo_v2.core.exception.common.ErrorCode;
import com.gamegoo.gamegoo_v2.utils.EmailTemplateProcessor;
import com.gamegoo.gamegoo_v2.utils.RandomCodeGeneratorUtil;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class EmailService {

    private final EmailVerifyRecordRepository emailVerifyRecordRepository;
    private final JavaMailSender javaMailSender;
    private static final String EMAIL_TEMPLATE_FOR_JOIN = "templates/email-template.html";
    private static final String CERTIFICATION_NUMBER = "CERTIFICATION_NUMBER";
    private static final String EMAIL_TITLE_FOR_JOIN = "GameGoo 이메일 인증 코드";

    /**
     * 이메일 인증 코드 검증
     *
     * @param email 검증용 이메일
     * @param code  검증용 코드
     */
    public void verifyEmailCode(String email, String code) {
        // 해당 이메일로 인증을 요청하지 않은 경우
        EmailVerifyRecord emailVerifyRecord = emailVerifyRecordRepository.findTop1ByEmailOrderByCreatedAtDesc(email)
                .orElseThrow(() -> new EmailException(ErrorCode.EMAIL_RECORD_NOT_FOUND));

        // 인증 코드가 틀린경우
        if (!emailVerifyRecord.getCode().equals(code)) {
            throw new EmailException(ErrorCode.INVALID_VERIFICATION_CODE);
        }

        // 인증 코드 발급 시간이 3분 초과인 경우
        if (emailVerifyRecord.getCreatedAt().isBefore(LocalDateTime.now().minusMinutes(3))) {
            throw new EmailException(ErrorCode.EMAIL_VERIFICATION_TIME_EXCEED);
        }
    }

    /**
     * 랜덤 코드 이메일 전송
     *
     * @param email 이메일
     */
    @Transactional
    public void sendEmailVerificationCode(String email) {
        // 해당 이메일로 3분 이내에 3번 이상 요청을 보냈을 경우 제한
        checkEmailSendRequestLimit(email);

        // 랜덤 코드 생성하기
        String certificationNumber = RandomCodeGeneratorUtil.generateEmailRandomCode();

        // Placeholder 값 정의
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put(CERTIFICATION_NUMBER, certificationNumber);

        // 이메일 전송
        sendEmail(email, EMAIL_TITLE_FOR_JOIN, EMAIL_TEMPLATE_FOR_JOIN, placeholders);

        // 이메일 전송 기록 DB 저장
        emailVerifyRecordRepository.save(EmailVerifyRecord.create(email, certificationNumber));
    }

    /**
     * 이메일 전송 메소드
     *
     * @param email        수신자 이메일
     * @param subject      이메일 제목
     * @param templatePath 템플릿 파일 경로
     * @param placeholders 템플릿에 삽입할 Placeholder 값
     */
    public void sendEmail(String email, String subject, String templatePath, Map<String, String> placeholders) {
        try {
            // 템플릿 처리
            String htmlContent = EmailTemplateProcessor.processTemplate(templatePath, placeholders);

            // MimeMessage 생성
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(message, true);
            mimeMessageHelper.setTo(email);
            mimeMessageHelper.setSubject(subject);
            mimeMessageHelper.setText(htmlContent, true);

            // 이메일 전송
            javaMailSender.send(message);

        } catch (Exception e) {
            log.error("이메일 전송에 실패했습니다.", e);
            throw new EmailException(ErrorCode.EMAIL_SEND_FAIL);
        }
    }

    /**
     * 특정 이메일로 3분 이내 3개 미만의 이메일을 보냈는지 검증
     *
     * @param email 이메일 주소
     */
    protected void checkEmailSendRequestLimit(String email) {
        LocalDateTime timeLimit = LocalDateTime.now().minusMinutes(3);

        // 3분 이내에 보낸 메일의 개수가 3개 이상일 경우 예외 처리
        if (emailVerifyRecordRepository.findRecentRecordsByEmail(email, timeLimit).size() > 2) {
            log.warn("3분 이내에 3개 이상의 이메일을 이미 보냈습니다. {}", email);
            throw new EmailException(ErrorCode.EMAIL_LIMIT_EXCEEDED);
        }
    }

}
