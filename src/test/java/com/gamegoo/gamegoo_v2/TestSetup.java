package com.gamegoo.gamegoo_v2;

import com.gamegoo.gamegoo_v2.social.manner.domain.MannerKeyword;
import com.gamegoo.gamegoo_v2.social.manner.repository.MannerKeywordRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@SpringBootTest
public class TestSetup {

    @Configuration
    static class TestConfig {

        @Bean
        CommandLineRunner initData(MannerKeywordRepository mannerKeywordRepository) {
            return args -> {
                if (mannerKeywordRepository.count() == 0) { // 데이터 중복 방지
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
            };
        }

    }

}
