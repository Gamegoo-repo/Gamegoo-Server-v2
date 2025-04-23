package com.gamegoo.gamegoo_v2.service.board;

import com.gamegoo.gamegoo_v2.account.member.domain.LoginType;
import com.gamegoo.gamegoo_v2.account.member.domain.Member;
import com.gamegoo.gamegoo_v2.account.member.domain.Mike;
import com.gamegoo.gamegoo_v2.account.member.domain.Position;
import com.gamegoo.gamegoo_v2.account.member.domain.Tier;
import com.gamegoo.gamegoo_v2.account.member.repository.MemberRepository;
import com.gamegoo.gamegoo_v2.content.board.domain.Board;
import com.gamegoo.gamegoo_v2.content.board.repository.BoardRepository;
import com.gamegoo.gamegoo_v2.content.board.service.BoardService;
import com.gamegoo.gamegoo_v2.matching.domain.GameMode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
public class BoardServiceTest {

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    BoardRepository boardRepository;

    @Autowired
    BoardService boardService;


    @AfterEach
    void tearDown() {
        boardRepository.deleteAllInBatch();
        memberRepository.deleteAllInBatch();
    }

    @Nested
    @DisplayName("해당 회원이 작성한 게시글 모두 삭제")
    class DeleteAllBoardByMemberTest {

        @DisplayName("해당 회원이 작성한 게시글이 없는 경우")
        @Test
        void deleteAllBoardByMemberWhenNoTarget() {
            // given
            Member member = createMember("member@gmail.com", "member");

            // when
            boardService.deleteAllBoardByMember(member);

            // then
            assertThat(boardRepository.findAll()).isEmpty();
        }

        @DisplayName("해당 회원이 작성한 게시글이 있는 경우")
        @Test
        void deleteAllBoardByMember() {
            // given
            Member member = createMember("member@gmail.com", "member");

            for (int i = 0; i < 3; i++) {
                Board board = Board.create(member, GameMode.ARAM, Position.ANY, Position.ANY, new ArrayList<>(),
                        Mike.AVAILABLE, "contents", 1);
                boardRepository.save(board);
            }

            // when
            boardService.deleteAllBoardByMember(member);

            // then
            List<Board> boards = boardRepository.findAll();
            assertThat(boards).allSatisfy(b -> assertThat(b.isDeleted()).isTrue());
        }

    }


    private Member createMember(String email, String gameName) {
        return memberRepository.save(Member.builder()
                .email(email)
                .password("testPassword")
                .profileImage(1)
                .loginType(LoginType.GENERAL)
                .gameName(gameName)
                .tag("TAG")
                .soloTier(Tier.IRON)
                .soloRank(0)
                .soloWinRate(0.0)
                .soloGameCount(0)
                .freeTier(Tier.IRON)
                .freeRank(0)
                .freeWinRate(0.0)
                .freeGameCount(0)
                .isAgree(true)
                .build());
    }

}
