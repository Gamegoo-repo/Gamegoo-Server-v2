package com.gamegoo.gamegoo_v2.repository.block;

import com.gamegoo.gamegoo_v2.block.domain.Block;
import com.gamegoo.gamegoo_v2.block.repository.BlockRepository;
import com.gamegoo.gamegoo_v2.member.domain.LoginType;
import com.gamegoo.gamegoo_v2.member.domain.Member;
import com.gamegoo.gamegoo_v2.member.domain.Tier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
class BlockRepositoryTest {

    @Autowired
    private BlockRepository blockRepository;

    @Autowired
    private TestEntityManager em;

    private static final int PAGE_SIZE = 10;

    private Member blocker;

    @BeforeEach
    void setUp() {
        blocker = createMember("test@gmail.com", "member");
    }

    @DisplayName("차단한 회원 목록 조회: 차단한 회원이 없는 경우")
    @Test
    void findBlockedMembersByBlockerIdAndNotDeleted() {
        // when
        Page<Member> blockedMembers = blockRepository.findBlockedMembersByBlockerIdAndNotDeleted(blocker.getId(),
                PageRequest.of(0, PAGE_SIZE));

        // then
        assertThat(blockedMembers).isEmpty();
        assertThat(blockedMembers.getTotalElements()).isEqualTo(0);
        assertThat(blockedMembers.getTotalPages()).isEqualTo(0);
        assertTrue(blockedMembers.isFirst());
        assertTrue(blockedMembers.isLast());
    }

    @DisplayName("차단한 회원 목록 조회: 차단한 회원이 10명 이하일 때 첫번째 페이지 요청")
    @Test
    void findBlockedMembersByBlockerIdAndNotDeletedOnePage() {
        // given
        for (int i = 1; i <= 10; i++) {
            Member blocked = createMember("member" + i + "@gmail.com", "member" + i);
            blockMember(blocker, blocked);
        }

        // when
        Page<Member> blockedMembers = blockRepository.findBlockedMembersByBlockerIdAndNotDeleted(blocker.getId(),
                PageRequest.of(0, PAGE_SIZE));

        // then
        assertThat(blockedMembers).isNotEmpty();
        assertThat(blockedMembers.getTotalElements()).isEqualTo(10);
        assertThat(blockedMembers.getTotalPages()).isEqualTo(1);
        assertTrue(blockedMembers.isFirst());
        assertTrue(blockedMembers.isLast());
    }

    @DisplayName("차단한 회원 목록 조회: 차단한 회원이 10명 초과일 때 두번째 페이지 요청")
    @Test
    void findBlockedMembersByBlockerIdAndNotDeletedSecondPage() {
        // given
        for (int i = 1; i <= 15; i++) {
            Member blocked = createMember("member" + i + "@gmail.com", "member" + i);
            blockMember(blocker, blocked);
        }

        // when
        Page<Member> blockedMembers = blockRepository.findBlockedMembersByBlockerIdAndNotDeleted(blocker.getId(),
                PageRequest.of(1, PAGE_SIZE));

        // then

        assertThat(blockedMembers).isNotEmpty();
        assertThat(blockedMembers.getTotalElements()).isEqualTo(15);
        assertThat(blockedMembers.getTotalPages()).isEqualTo(2);
        assertFalse(blockedMembers.isFirst());
        assertTrue(blockedMembers.isLast());
    }

    private Member createMember(String email, String gameName) {
        return em.persist(Member.builder()
                .email(email)
                .password("testPassword")
                .profileImage(1)
                .loginType(LoginType.GENERAL)
                .gameName(gameName)
                .tag("TAG")
                .tier(Tier.IRON)
                .gameRank(0)
                .winRate(0.0)
                .gameCount(0)
                .isAgree(true)
                .build());
    }

    private void blockMember(Member blocker, Member blocked) {
        Block block = Block.create(blocker, blocked);
        em.persist(block);
    }

}