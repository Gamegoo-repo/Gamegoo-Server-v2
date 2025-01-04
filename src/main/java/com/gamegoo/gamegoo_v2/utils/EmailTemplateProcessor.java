package com.gamegoo.gamegoo_v2.utils;

import com.gamegoo.gamegoo_v2.core.exception.EmailException;
import com.gamegoo.gamegoo_v2.core.exception.common.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.util.FileCopyUtils;

import java.nio.charset.StandardCharsets;
import java.util.Map;

@Slf4j
@Component
public class EmailTemplateProcessor {

    /**
     * 동적으로 템플릿을 선택하고 값을 대체
     *
     * @param templateName  템플릿 파일 이름 (예: test.txt)
     * @param placeholders  대체할 값 (key: placeholder, value: 대체 값)
     * @return              최종 HTML 문자열
     */
    public static String processTemplate(String templateName, Map<String, String> placeholders) {
        try {
            // 템플릿 파일 읽기
            ClassPathResource resource = new ClassPathResource(templateName);
            byte[] bdata = FileCopyUtils.copyToByteArray(resource.getInputStream());
            String templateContent = new String(bdata, StandardCharsets.UTF_8);

            // Placeholder 대체
            for (Map.Entry<String, String> entry : placeholders.entrySet()) {
                templateContent = templateContent.replace("{{" + entry.getKey() + "}}", entry.getValue());
            }

            return templateContent;
        } catch (Exception e) {
            throw new EmailException(ErrorCode.EMAIL_CONTENT_LOAD_FAIL);
        }
    }

}
