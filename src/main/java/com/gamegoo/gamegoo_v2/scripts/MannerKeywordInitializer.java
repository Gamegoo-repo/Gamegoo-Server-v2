package com.gamegoo.gamegoo_v2.scripts;

import com.gamegoo.gamegoo_v2.social.manner.domain.MannerKeyword;
import com.gamegoo.gamegoo_v2.social.manner.repository.MannerKeywordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class MannerKeywordInitializer implements ApplicationListener<ApplicationReadyEvent> {

    private final MannerKeywordRepository mannerKeywordRepository;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        if (isCreateMode(event)) {
            try {
                initializeMannerKeywords();
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

    private void initializeMannerKeywords() throws IOException {
        List<MannerKeyword> mannerKeywords = new ArrayList<>();

        mannerKeywords.add(MannerKeyword.create("캐리했어요", true));
        mannerKeywords.add(MannerKeyword.create("1인분 이상은 해요", true));
        mannerKeywords.add(MannerKeyword.create("욕 안해요", true));
        mannerKeywords.add(MannerKeyword.create("남탓 안해요", true));
        mannerKeywords.add(MannerKeyword.create("매너 있어요", true));
        mannerKeywords.add(MannerKeyword.create("답장 빠름", true));
        mannerKeywords.add(MannerKeyword.create("탈주", false));
        mannerKeywords.add(MannerKeyword.create("욕설", false));
        mannerKeywords.add(MannerKeyword.create("고의 트롤", false));
        mannerKeywords.add(MannerKeyword.create("대리 사용자", false));
        mannerKeywords.add(MannerKeyword.create("소환사명 불일치", false));
        mannerKeywords.add(MannerKeyword.create("답장이 없어요", false));

        mannerKeywordRepository.saveAll(mannerKeywords);
    }

}
