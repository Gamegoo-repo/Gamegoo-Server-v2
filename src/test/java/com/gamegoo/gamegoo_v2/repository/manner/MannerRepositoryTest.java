package com.gamegoo.gamegoo_v2.repository.manner;

import com.gamegoo.gamegoo_v2.account.member.domain.Member;
import com.gamegoo.gamegoo_v2.repository.RepositoryTestSupport;
import com.gamegoo.gamegoo_v2.social.manner.domain.MannerKeyword;
import com.gamegoo.gamegoo_v2.social.manner.domain.MannerRating;
import com.gamegoo.gamegoo_v2.social.manner.domain.MannerRatingKeyword;
import com.gamegoo.gamegoo_v2.social.manner.repository.MannerKeywordRepository;
import com.gamegoo.gamegoo_v2.social.manner.repository.MannerRatingKeywordRepository;
import com.gamegoo.gamegoo_v2.social.manner.repository.MannerRatingRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class MannerRepositoryTest extends RepositoryTestSupport {

    @Autowired
    MannerRatingKeywordRepository mannerRatingKeywordRepository;

    @Autowired
    MannerKeywordRepository mannerKeywordRepository;

    @Autowired
    MannerRatingRepository mannerRatingRepository;

    private final List<Long> mannerKeywordIds = List.of(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L, 11L, 12L);

    @Nested
    @DisplayName("회원이 받은 매너 키워드 별 개수 조회")
    class CountMannerKeywordByToMemberIdTest {

        @DisplayName("받은 매너 키워드가 없는 경우")
        @Test
        void countMannerKeywordByToMemberIdWhenNoMannerKeyword() {
            // when
            Map<Long, Integer> resultMap = mannerRatingKeywordRepository.countMannerKeywordByToMemberId(member.getId());

            // then
            assertThat(resultMap)
                    .isNotNull()
                    .hasSize(mannerKeywordIds.size())
                    .containsKeys(mannerKeywordIds.toArray(new Long[0]))
                    .allSatisfy((key, value) -> assertThat(value).isEqualTo(0));
        }

        @DisplayName("받은 매너 키워드가 있는 경우")
        @Test
        void countMannerKeywordByToMemberId() {
            // given
            initMannerKeyword();
            Member targetMember1 = createMember("targetMember1@gmail.com", "targetMember1");
            Member targetMember2 = createMember("targetMember2@gmail.com", "targetMember2");
            Member targetMember3 = createMember("targetMember3@gmail.com", "targetMember3");

            createMannerRating(List.of(1L, 2L, 3L, 4L, 5L, 6L), targetMember1, member, true);
            createMannerRating(List.of(1L, 2L, 3L), targetMember2, member, true);
            createMannerRating(List.of(7L, 8L), targetMember3, member, false);

            // when
            Map<Long, Integer> resultMap = mannerRatingKeywordRepository.countMannerKeywordByToMemberId(member.getId());

            // then
            assertThat(resultMap)
                    .isNotNull()
                    .hasSize(mannerKeywordIds.size())
                    .containsKeys(mannerKeywordIds.toArray(new Long[0]));

            assertThat(resultMap.get(1L)).isEqualTo(2);
            assertThat(resultMap.get(2L)).isEqualTo(2);
            assertThat(resultMap.get(3L)).isEqualTo(2);
            assertThat(resultMap.get(4L)).isEqualTo(1);
            assertThat(resultMap.get(5L)).isEqualTo(1);
            assertThat(resultMap.get(6L)).isEqualTo(1);
            assertThat(resultMap.get(7L)).isEqualTo(1);
            assertThat(resultMap.get(8L)).isEqualTo(1);
            assertThat(resultMap.get(9L)).isEqualTo(0);
            assertThat(resultMap.get(10L)).isEqualTo(0);
            assertThat(resultMap.get(11L)).isEqualTo(0);
            assertThat(resultMap.get(12L)).isEqualTo(0);
        }

    }

    private MannerRating createMannerRating(List<Long> mannerKeywordIds, Member member, Member targetMember,
                                            boolean positive) {
        // 매너 키워드 엔티티 조회
        List<MannerKeyword> mannerKeywordList = mannerKeywordRepository.findAllById(mannerKeywordIds);

        // MannerRating 엔티티 생성 및 저장
        MannerRating mannerRating = mannerRatingRepository.save(MannerRating.create(member, targetMember, positive));

        // MannerRatingKeyword 엔티티 생성 및 저장
        List<MannerRatingKeyword> mannerRatingKeywordList = mannerKeywordList.stream()
                .map(mannerKeyword -> MannerRatingKeyword.create(mannerRating, mannerKeyword))
                .toList();
        mannerRatingKeywordRepository.saveAll(mannerRatingKeywordList);

        return mannerRating;
    }

    private void initMannerKeyword() {
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

}
