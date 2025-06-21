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
import com.gamegoo.gamegoo_v2.core.exception.BoardException;
import com.gamegoo.gamegoo_v2.core.exception.common.ErrorCode;
import com.gamegoo.gamegoo_v2.matching.domain.GameMode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Slice;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
public class BoardServiceTest {

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    BoardRepository boardRepository;

    @Autowired
    BoardService boardService;

    @BeforeEach
    void cleanUp() {
        boardRepository.deleteAll();
    }

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

    @Nested
    @DisplayName("게시글 끌올")
    class BumpBoardTest {

        @Test
        @DisplayName("게시글 끌올 성공")
        void bumpBoardSuccess() {
            // given
            Member member = createMember("member@gmail.com", "member");
            Board board = Board.create(member, GameMode.ARAM, Position.ANY, Position.ANY, new ArrayList<>(),
                    Mike.AVAILABLE, "contents", 1);
            boardRepository.save(board);

            // when
            Board result = boardService.bumpBoard(board.getId(), member.getId());

            // then
            assertThat(result.getBumpTime()).isNotNull();
        }

        @Test
        @DisplayName("게시글 끌올 실패 - 권한 없음")
        void bumpBoardFailNoPermission() {
            // given
            Member member1 = createMember("member1@gmail.com", "member1");
            Member member2 = createMember("member2@gmail.com", "member2");
            Board board = Board.create(member1, GameMode.ARAM, Position.ANY, Position.ANY, new ArrayList<>(),
                    Mike.AVAILABLE, "contents", 1);
            boardRepository.save(board);

            // when & then
            assertThatThrownBy(() -> boardService.bumpBoard(board.getId(), member2.getId()))
                    .isInstanceOf(BoardException.class)
                    .hasFieldOrPropertyWithValue("code", ErrorCode.BUMP_ACCESS_DENIED.getCode());
        }

        @Test
        @DisplayName("게시글 끌올 실패 - 5분 이내 재시도")
        void bumpBoardFailTimeLimit() {
            // given
            Member member = createMember("member@gmail.com", "member");
            Board board = Board.create(member, GameMode.ARAM, Position.ANY, Position.ANY, new ArrayList<>(),
                    Mike.AVAILABLE, "contents", 1);
            boardRepository.save(board);
            board.bump(LocalDateTime.now());
            boardRepository.save(board);

            // when & then
            assertThatThrownBy(() -> boardService.bumpBoard(board.getId(), member.getId()))
                    .isInstanceOf(BoardException.class)
                    .hasFieldOrPropertyWithValue("code", ErrorCode.BUMP_TIME_LIMIT.getCode());
        }

        @Test
        @DisplayName("게시글 끌올 실패 - 게시글 없음")
        void bumpBoardFailNotFound() {
            // given
            Member member = createMember("member@gmail.com", "member");

            // when & then
            assertThatThrownBy(() -> boardService.bumpBoard(999L, member.getId()))
                    .isInstanceOf(BoardException.class)
                    .hasFieldOrPropertyWithValue("code", ErrorCode.BOARD_NOT_FOUND.getCode());
        }
    }

    @Nested
    @DisplayName("내가 작성한 게시글 커서 기반 조회")
    class GetMyBoardsWithCursorTest {

        @Test
        @DisplayName("커서가 null일 경우 최신 게시글부터 조회")
        void getMyBoardsWithNullCursor() {
            // given
            Member member = createMember("member@gmail.com", "member");
            memberRepository.save(member);
            List<Board> boards = new ArrayList<>();
            LocalDateTime baseTime = LocalDateTime.now();

            // 게시글 5개 생성 (시간차를 두고)
            for (int i = 0; i < 5; i++) {
                Board board = Board.create(member, GameMode.ARAM, Position.ANY, Position.ANY, new ArrayList<>(),
                        Mike.AVAILABLE, "contents " + i, 1);
                ReflectionTestUtils.setField(board, "createdAt", baseTime.minusMinutes(i));
                if (i % 2 == 0) { // 일부 게시글만 bump
                    board.bump(baseTime.plusMinutes(i));
                }
                boards.add(boardRepository.save(board));
            }

            // when
            var result = boardService.getMyBoards(member.getId(), null);

            // then
            assertThat(result.getContent()).hasSize(5);
            assertThat(result.hasNext()).isFalse();
            // 최신순 정렬 확인 (activityTime 기준)
            assertThat(result.getContent()).isSortedAccordingTo(
                (b1, b2) -> {
                    LocalDateTime t1 = b1.getBumpTime() != null ? b1.getBumpTime() : b1.getCreatedAt();
                    LocalDateTime t2 = b2.getBumpTime() != null ? b2.getBumpTime() : b2.getCreatedAt();
                    return t2.compareTo(t1);
                }
            );
        }

        @Test
        @DisplayName("커서 기반 페이징이 정상 동작하는지 확인")
        void getMyBoardsWithCursor() {
            // given
            Member member = createMember("member@gmail.com", "member");
            memberRepository.save(member);
            List<Board> boards = new ArrayList<>();
            LocalDateTime baseTime = LocalDateTime.now();

            // 게시글 15개 생성
            for (int i = 0; i < 15; i++) {
                Board board = Board.create(member, GameMode.ARAM, Position.ANY, Position.ANY, new ArrayList<>(),
                        Mike.AVAILABLE, "contents " + i, 1);
                ReflectionTestUtils.setField(board, "createdAt", baseTime.minusMinutes(i));
                if (i % 3 == 0) { // 일부 게시글만 bump
                    board.bump(baseTime.plusMinutes(i));
                }
                boards.add(boardRepository.save(board));
            }


            // when - 첫 페이지 조회
            var firstPage = boardService.getMyBoards(member.getId(), null);

            // then - 첫 페이지 검증
            assertThat(firstPage.getContent()).hasSize(10);
            assertThat(firstPage.hasNext()).isTrue();

            // when - 두 번째 페이지 조회
            var lastActivityTime = firstPage.getContent().get(firstPage.getContent().size() - 1).getBumpTime() != null ?
                firstPage.getContent().get(firstPage.getContent().size() - 1).getBumpTime() :
                firstPage.getContent().get(firstPage.getContent().size() - 1).getCreatedAt();
            var secondPage = boardService.getMyBoards(member.getId(), lastActivityTime);

            // then - 두 번째 페이지 검증
            System.out.println("secondPage.getContent().size() = " + secondPage.getContent().size());
            for (Board board : secondPage.getContent()) {
                LocalDateTime activityTime = board.getBumpTime() != null ? board.getBumpTime() : board.getCreatedAt();
                System.out.println("activityTime = " + activityTime + ", gameMode = " + board.getGameMode() + ", mainP = " + board.getMainP() + ", subP = " + board.getSubP());
            }
            assertThat(secondPage.getContent()).hasSize(5);
            assertThat(secondPage.hasNext()).isFalse();
            assertThat(secondPage.getContent()).allSatisfy(board -> {
                LocalDateTime activityTime = board.getBumpTime() != null ? board.getBumpTime() : board.getCreatedAt();
                assertThat(activityTime).isBefore(lastActivityTime);
            });
        }

        @Test
        @DisplayName("삭제된 게시글은 조회되지 않아야 함")
        void getMyBoardsExcludeDeleted() {
            // given
            Member member = createMember("member@gmail.com", "member");
            memberRepository.save(member);
            List<Board> boards = new ArrayList<>();
            LocalDateTime baseTime = LocalDateTime.now();

            // 게시글 5개 생성 (일부는 삭제 처리)
            for (int i = 0; i < 5; i++) {
                Board board = Board.create(member, GameMode.ARAM, Position.ANY, Position.ANY, new ArrayList<>(),
                        Mike.AVAILABLE, "contents " + i, 1);
                ReflectionTestUtils.setField(board, "createdAt", baseTime.minusMinutes(i));
                if (i % 2 == 0) {
                    board.setDeleted(true);
                }
                boards.add(boardRepository.save(board));
            }

            // when
            var result = boardService.getMyBoards(member.getId(), null);

            // then
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getContent()).allSatisfy(board ->
                assertThat(board.isDeleted()).isFalse()
            );
        }
    }

    @Nested
    @DisplayName("전체 게시물 커서 기반 조회")
    class GetAllBoardsWithCursorTest {

        @Test
        @DisplayName("첫 페이지 조회 - 필터 없음")
        void getAllBoardsFirstPage() {
            // given
            Member member = createMember("member@gmail.com", "member");
            memberRepository.save(member);
            List<Board> boards = new ArrayList<>();
            LocalDateTime baseTime = LocalDateTime.now();

            // 게시글 10개 생성
            for (int i = 0; i < 10; i++) {
                Board board = Board.create(member, GameMode.SOLO, Position.TOP, Position.JUNGLE, new ArrayList<>(),
                        Mike.AVAILABLE, "contents " + i, 1);
                ReflectionTestUtils.setField(board, "createdAt", baseTime.minusMinutes(i));
                if (i % 2 == 0) {
                    board.bump(baseTime.plusMinutes(i));
                }
                boards.add(boardRepository.save(board));
            }

            // when
            var result = boardService.getAllBoardsWithCursor(null, null, null, null, null, null);

            // then
            assertThat(result.getContent()).hasSize(10);
            assertThat(result.hasNext()).isFalse();
            assertThat(result.getContent()).isSortedAccordingTo(
                (b1, b2) -> {
                    LocalDateTime t1 = b1.getBumpTime() != null ? b1.getBumpTime() : b1.getCreatedAt();
                    LocalDateTime t2 = b2.getBumpTime() != null ? b2.getBumpTime() : b2.getCreatedAt();
                    return t2.compareTo(t1);
                }
            );
        }

        @Test
        @DisplayName("두 번째 페이지 조회 - 필터 적용")
        void getAllBoardsSecondPage() {
            // given
            Member member = createMember("member@gmail.com", "member");
            ReflectionTestUtils.setField(member, "soloTier", Tier.GOLD);
            memberRepository.save(member);
            List<Board> boards = new ArrayList<>();
            LocalDateTime baseTime = LocalDateTime.now();

            // 게시글 15개 생성 (모두 SOLO, TOP, JUNGLE, TIER.GOLD)
            for (int i = 0; i < 15; i++) {
                Board board = Board.create(member, GameMode.SOLO, Position.TOP, Position.JUNGLE, new ArrayList<>(),
                        Mike.AVAILABLE, "contents " + i, 1);
                LocalDateTime createdAt = baseTime.minusMinutes(i).minusSeconds(i);
                ReflectionTestUtils.setField(board, "createdAt", createdAt);
                if (i % 3 == 0) {
                    board.bump(baseTime.plusMinutes(i).plusSeconds(i));
                }
                boards.add(boardRepository.save(board));
            }

            // 첫 페이지 조회
            Slice<Board> firstPage = boardService.getAllBoardsWithCursor(null, null, GameMode.SOLO, Tier.GOLD, Position.TOP, Position.JUNGLE);
            assertThat(firstPage.getContent()).hasSize(10);
            assertThat(firstPage.hasNext()).isTrue();

            // 두 번째 페이지 조회
            Board lastBoard = firstPage.getContent().get(firstPage.getContent().size() - 1);
            LocalDateTime lastActivityTime = lastBoard.getBumpTime() != null ? lastBoard.getBumpTime() : lastBoard.getCreatedAt();
            Long lastId = lastBoard.getId();
            Slice<Board> secondPage = boardService.getAllBoardsWithCursor(lastActivityTime, lastId, GameMode.SOLO, Tier.GOLD, Position.TOP, Position.JUNGLE);

            // then
            assertThat(secondPage.getContent()).hasSize(5);
            assertThat(secondPage.hasNext()).isFalse();
            assertThat(secondPage.getContent()).allSatisfy(board -> {
                LocalDateTime activityTime = board.getBumpTime() != null ? board.getBumpTime() : board.getCreatedAt();
                assertThat(activityTime).isBefore(lastActivityTime);
                assertThat(board.getGameMode()).isEqualTo(GameMode.SOLO);
                assertThat(board.getMainP()).isEqualTo(Position.TOP);
                assertThat(board.getSubP()).isEqualTo(Position.JUNGLE);
            });
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
