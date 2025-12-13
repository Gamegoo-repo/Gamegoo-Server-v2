package com.gamegoo.gamegoo_v2.repository.board;

import com.gamegoo.gamegoo_v2.account.member.domain.Member;
import com.gamegoo.gamegoo_v2.account.member.domain.Mike;
import com.gamegoo.gamegoo_v2.account.member.domain.Position;
import com.gamegoo.gamegoo_v2.account.member.domain.Tier;
import com.gamegoo.gamegoo_v2.content.board.domain.Board;
import com.gamegoo.gamegoo_v2.content.board.repository.BoardRepository;
import com.gamegoo.gamegoo_v2.matching.domain.GameMode;
import com.gamegoo.gamegoo_v2.repository.RepositoryTestSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class BoardRepositoryTest extends RepositoryTestSupport {

    @Autowired
    private BoardRepository boardRepository;

    private Board createBoard(Member member, GameMode gameMode, Position mainP, Position subP, Mike mike) {
        return em.persist(Board.builder()
                .member(member)
                .gameMode(gameMode)
                .mainP(mainP)
                .subP(subP)
                .mike(mike)
                .content("Test content")
                .wantP(new ArrayList<>())
                .build());
    }

    @Nested
    @DisplayName("기본 CRUD 테스트")
    class CrudTest {
        
        @Test
        @DisplayName("게시글 저장 및 조회")
        void saveAndFind() {
            // given
            Board board = createBoard(member, GameMode.SOLO, Position.TOP, Position.MID, Mike.AVAILABLE);

            // when
            Optional<Board> found = boardRepository.findById(board.getId());

            // then
            assertThat(found).isPresent();
            assertThat(found.get().getMember()).isEqualTo(member);
            assertThat(found.get().getGameMode()).isEqualTo(GameMode.SOLO);
        }

        @Test
        @DisplayName("게시글 삭제 플래그 설정")
        void softDelete() {
            // given
            Board board = createBoard(member, GameMode.SOLO, Position.TOP, Position.MID, Mike.AVAILABLE);

            // when
            board.setDeleted(true);
            em.flush();
            em.clear();

            // then
            Optional<Board> found = boardRepository.findByIdAndDeleted(board.getId(), false);
            assertThat(found).isEmpty();
        }
    }

    @Nested
    @DisplayName("게시글 필터링 테스트")
    class FilteringTest {

        private Member ironMember;
        private Member goldMember;
        private Member platinumMember;

        @BeforeEach
        void setUp() {
            // 다양한 티어의 멤버 생성
            ironMember = createMember("iron@test.com", "iron");
            goldMember = createMember("gold@test.com", "gold");
            platinumMember = createMember("platinum@test.com", "platinum");
            
            // 티어 설정
            ReflectionTestUtils.setField(goldMember, "soloTier", Tier.GOLD);
            ReflectionTestUtils.setField(platinumMember, "soloTier", Tier.PLATINUM);
            
            // 포지션과 마이크 설정
            goldMember.updateMemberByMatchingRecord(Mike.AVAILABLE, Position.TOP, Position.MID, new ArrayList<>());
            platinumMember.updateMemberByMatchingRecord(Mike.AVAILABLE, Position.ADC, Position.SUP, new ArrayList<>());
            
            // 매너 점수 설정
            goldMember.updateMannerScore(100);
            platinumMember.updateMannerScore(150);

            // 23개의 게시물 생성
            createTestPosts();
        }

        private void createTestPosts() {
            LocalDateTime baseTime = LocalDateTime.now();
            Position[] positions = {Position.TOP, Position.JUNGLE, Position.MID, Position.ADC, Position.SUP};
            
            // IRON 멤버의 게시물 (7개)
            for (int i = 0; i < 7; i++) {
                Board board = createBoard(ironMember, 
                    i % 2 == 0 ? GameMode.SOLO : GameMode.FREE,
                    positions[i % positions.length],
                    positions[(i + 1) % positions.length],
                    i % 3 == 0 ? Mike.AVAILABLE : Mike.UNAVAILABLE);
                ReflectionTestUtils.setField(board, "createdAt", baseTime.minusHours(i));
            }

            // GOLD 멤버의 게시물 (8개)
            for (int i = 0; i < 8; i++) {
                Board board = createBoard(goldMember,
                    i % 2 == 0 ? GameMode.SOLO : GameMode.ARAM,
                    positions[i % positions.length],
                    positions[(i + 2) % positions.length],
                    i % 2 == 0 ? Mike.AVAILABLE : Mike.UNAVAILABLE);
                ReflectionTestUtils.setField(board, "createdAt", baseTime.minusHours(i + 7));
            }

            // PLATINUM 멤버의 게시물 (8개)
            for (int i = 0; i < 8; i++) {
                Board board = createBoard(platinumMember,
                    i % 3 == 0 ? GameMode.SOLO : (i % 3 == 1 ? GameMode.FREE : GameMode.ARAM),
                    positions[i % positions.length],
                    positions[(i + 3) % positions.length],
                    i % 2 == 0 ? Mike.AVAILABLE : Mike.UNAVAILABLE);
                ReflectionTestUtils.setField(board, "createdAt", baseTime.minusHours(i + 15));
            }

            em.flush();
            em.clear();
        }

        @Test
        @DisplayName("게임 모드와 티어로 필터링")
        void filterByGameModeAndTier() {
            // when
            Page<Board> result = boardRepository.findByGameModeAndTierAndMainPInAndSubPInAndMikeAndDeletedFalse(
                    GameMode.SOLO,
                    Tier.GOLD,
                    Arrays.asList(Position.TOP),  // 주 포지션 또는 부 포지션이 TOP인 게시물
                    Mike.AVAILABLE,
                    PageRequest.of(0, 10)
            );

            // then
            assertThat(result.getContent())
                .isNotEmpty()
                .allSatisfy(board -> {
                    assertThat(board.getGameMode()).isEqualTo(GameMode.SOLO);
                    assertThat(board.getMember().getSoloTier()).isEqualTo(Tier.GOLD);
                    // 주 포지션 또는 부 포지션이 TOP이어야 함
                    assertThat(board.getMainP() == Position.TOP || board.getSubP() == Position.TOP).isTrue();
                    assertThat(board.getMike()).isEqualTo(Mike.AVAILABLE);
                });
        }

        @Test
        @DisplayName("여러 페이지에 걸친 게시글 조회")
        void findMultiplePages() {
            Position[] positions = {Position.TOP, Position.JUNGLE, Position.MID, Position.ADC, Position.SUP};

            // when
            Page<Board> firstPage = boardRepository.findByGameModeAndTierAndMainPInAndSubPInAndMikeAndDeletedFalse(
                    GameMode.SOLO,
                    null,
                    Arrays.asList(positions),  // 주 포지션 또는 부 포지션이 이 중 하나인 게시물
                    null,
                    PageRequest.of(0, 10)
            );

            Page<Board> secondPage = boardRepository.findByGameModeAndTierAndMainPInAndSubPInAndMikeAndDeletedFalse(
                    GameMode.SOLO,
                    null,
                    Arrays.asList(positions),  // 주 포지션 또는 부 포지션이 이 중 하나인 게시물
                    null,
                    PageRequest.of(1, 10)
            );

            // then
            assertThat(firstPage.getContent()).hasSize(10);
            assertThat(secondPage.getContent()).isNotEmpty();
            assertThat(firstPage.getContent())
                .extracting(Board::getCreatedAt)
                .isSortedAccordingTo((d1, d2) -> d2.compareTo(d1));
        }
    }

    @Nested
    @DisplayName("활동 시간 기반 정렬 테스트")
    class ActivityTimeTest {

        @Test
        @DisplayName("끌올 시간이 있는 경우 활동 시간 기준으로 정렬")
        void sortByActivityTimeWithBump() {
            // given
            Board oldBoard = createBoard(member, GameMode.SOLO, Position.TOP, Position.MID, Mike.AVAILABLE);
            Board newBoard = createBoard(member, GameMode.SOLO, Position.TOP, Position.MID, Mike.AVAILABLE);
            
            // 오래된 게시글을 끌올
            LocalDateTime bumpTime = LocalDateTime.now().plusHours(1);
            oldBoard.bump(bumpTime);
            
            em.flush();
            em.clear();

            // when
            Page<Board> result = boardRepository.findByMemberIdAndDeletedFalse(
                    member.getId(),
                    PageRequest.of(0, 10)
            );

            // then
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getContent().get(0).getId()).isEqualTo(oldBoard.getId());
            assertThat(result.getContent().get(1).getId()).isEqualTo(newBoard.getId());
        }
    }

    @Nested
    @DisplayName("커서 기반 페이지네이션 테스트")
    class CursorPaginationTest {

        @Test
        @DisplayName("내 게시물 커서 기반 조회 - 첫 페이지")
        void getMyBoardsFirstPage() {
            // given
            Member testMember = createMember("test2@test.com", "tester");
            LocalDateTime baseTime = LocalDateTime.now();
            
            // 15개의 게시물 생성
            for (int i = 0; i < 15; i++) {
                Board board = createBoard(testMember, GameMode.SOLO, Position.TOP, Position.MID, Mike.AVAILABLE);
                ReflectionTestUtils.setField(board, "createdAt", baseTime.minusHours(i));
                
                // 일부 게시물은 끌올 처리
                if (i % 3 == 0) {
                    board.bump(baseTime.plusMinutes(i));
                }
            }
            
            em.flush();
            em.clear();

            // when
            Slice<Board> result = boardRepository.findByMemberIdAndActivityTimeLessThan(
                    testMember.getId(),
                    null,
                    PageRequest.of(0, 10)
            );

            // then
            assertThat(result.getContent()).hasSize(10); // 첫 페이지는 10개
            assertThat(result.hasNext()).isTrue(); // 다음 페이지 존재
            assertThat(result.getContent())
                .extracting(Board::getActivityTime)
                .isSortedAccordingTo((t1, t2) -> t2.compareTo(t1)); // 활동 시간 내림차순 정렬
        }

        @Test
        @DisplayName("내 게시물 커서 기반 조회 - 두 번째 페이지")
        void getMyBoardsSecondPage() {
            // given
            Member testMember = createMember("test2@test.com", "tester");
            LocalDateTime baseTime = LocalDateTime.now();
            List<Board> boards = new ArrayList<>();
            
            // 15개의 게시물 생성
            for (int i = 0; i < 15; i++) {
                Board board = createBoard(testMember, GameMode.SOLO, Position.TOP, Position.MID, Mike.AVAILABLE);
                ReflectionTestUtils.setField(board, "createdAt", baseTime.minusHours(i));
                
                // 일부 게시물은 끌올 처리
                if (i % 3 == 0) {
                    board.bump(baseTime.plusMinutes(i));
                }
                boards.add(board);
            }
            
            em.flush();
            em.clear();

            // 첫 페이지 조회
            Slice<Board> firstPage = boardRepository.findByMemberIdAndActivityTimeLessThan(
                    testMember.getId(),
                    null,
                    PageRequest.of(0, 10)
            );

            // when - 두 번째 페이지 조회
            LocalDateTime cursor = firstPage.getContent().get(firstPage.getContent().size() - 1).getActivityTime();
            Slice<Board> secondPage = boardRepository.findByMemberIdAndActivityTimeLessThan(
                    testMember.getId(),
                    cursor,
                    PageRequest.of(0, 10)
            );

            // then
            assertThat(secondPage.getContent()).hasSize(5); // 남은 5개
            assertThat(secondPage.hasNext()).isFalse(); // 다음 페이지 없음
            
            // 페이지 간 연속성 검증
            assertThat(firstPage.getContent().get(firstPage.getContent().size() - 1).getActivityTime())
                .isAfterOrEqualTo(secondPage.getContent().get(0).getActivityTime());
            
            // 두 번째 페이지 정렬 검증
            assertThat(secondPage.getContent())
                .extracting(Board::getActivityTime)
                .isSortedAccordingTo((t1, t2) -> t2.compareTo(t1));
        }

        @Test
        @DisplayName("내 게시물 커서 기반 조회 - 끌올이 섞인 경우")
        void getMyBoardsWithBumpMixed() {
            // given
            Member testMember = createMember("test2@test.com", "tester");
            LocalDateTime baseTime = LocalDateTime.now();
            List<Board> boards = new ArrayList<>();
            
            // 12개의 게시물 생성
            for (int i = 0; i < 12; i++) {
                Board board = createBoard(testMember, GameMode.SOLO, Position.TOP, Position.MID, Mike.AVAILABLE);
                ReflectionTestUtils.setField(board, "createdAt", baseTime.minusHours(i));
                boards.add(board);
            }
            
            // 중간 게시물 3개를 끌올
            boards.get(5).bump(baseTime.plusMinutes(30)); // 5번째 게시물
            boards.get(8).bump(baseTime.plusMinutes(20)); // 8번째 게시물
            boards.get(10).bump(baseTime.plusMinutes(10)); // 10번째 게시물
            
            em.flush();
            em.clear();

            // when
            Slice<Board> result = boardRepository.findByMemberIdAndActivityTimeLessThan(
                    testMember.getId(),
                    null,
                    PageRequest.of(0, 10)
            );

            // then
            assertThat(result.getContent()).hasSize(10);
            
            // 끌올된 게시물이 상단에 위치하는지 확인
            List<LocalDateTime> activityTimes = result.getContent().stream()
                .map(Board::getActivityTime)
                .collect(Collectors.toList());
            
            assertThat(activityTimes.get(0)).isAfter(baseTime); // 첫 번째는 끌올된 게시물
            assertThat(activityTimes)
                .isSortedAccordingTo((t1, t2) -> t2.compareTo(t1)); // 활동 시간 내림차순 정렬
        }

        @Test
        @DisplayName("내 게시물 커서 기반 조회 - 삭제된 게시물 제외")
        void getMyBoardsExcludeDeleted() {
            // given
            Member testMember = createMember("test2@test.com", "tester");
            LocalDateTime baseTime = LocalDateTime.now();
            
            // 10개의 게시물 생성
            for (int i = 0; i < 10; i++) {
                Board board = createBoard(testMember, GameMode.SOLO, Position.TOP, Position.MID, Mike.AVAILABLE);
                ReflectionTestUtils.setField(board, "createdAt", baseTime.minusHours(i));
                
                // 3의 배수 인덱스의 게시물은 삭제 처리 (0, 3, 6, 9)
                if (i % 3 == 0) {
                    board.setDeleted(true);
                }
                em.persist(board);
            }
            
            em.flush();
            em.clear();

            // when
            Slice<Board> result = boardRepository.findByMemberIdAndActivityTimeLessThan(
                    testMember.getId(),
                    null,
                    PageRequest.of(0, 10)
            );

            // then
            assertThat(result.getContent()).hasSize(6); // 삭제되지 않은 6개만 조회 (인덱스: 1, 2, 4, 5, 7, 8)
            assertThat(result.getContent())
                .allSatisfy(board -> assertThat(board.isDeleted()).isFalse());
            assertThat(result.getContent())
                .extracting(Board::getActivityTime)
                .isSortedAccordingTo((t1, t2) -> t2.compareTo(t1));
        }
    }

    @Nested
    @DisplayName("전체 게시물 커서 기반 조회 테스트")
    class AllBoardsCursorTest {

        @BeforeEach
        void cleanUp() {
            boardRepository.deleteAll();
        }

        @Test
        @DisplayName("첫 페이지 조회 - 필터 없음")
        void getAllBoardsFirstPage() {
            // given
            Member testMember = createMember("test@test.com", "tester");
            LocalDateTime baseTime = LocalDateTime.now();
            List<Board> boards = new ArrayList<>();
            // 15개의 게시물 생성 (SOLO, TOP, MID)
            for (int i = 0; i < 15; i++) {
                Board board = createBoard(testMember, GameMode.SOLO, Position.TOP, Position.MID, Mike.AVAILABLE);
                LocalDateTime createdAt = baseTime.minusHours(i).minusMinutes(i).minusSeconds(i);
                ReflectionTestUtils.setField(board, "createdAt", createdAt);
                ReflectionTestUtils.setField(board, "activityTime", createdAt);
                if (i % 3 == 0) {
                    LocalDateTime bumpTime = baseTime.plusMinutes(i).plusSeconds(i);
                    board.bump(bumpTime);
                    ReflectionTestUtils.setField(board, "activityTime", bumpTime);
                }
                em.persist(board);
                boards.add(board);
            }
            em.flush();
            em.clear();

            Pageable pageable = PageRequest.of(0, 10);
            List<Position> positionList = List.of(Position.TOP);  // 주 포지션 또는 부 포지션이 TOP인 게시물
            Slice<Board> firstPage = boardRepository.findAllBoardsWithCursor(null, null, GameMode.SOLO, null, positionList, null, pageable);

            // then
            assertThat(firstPage.getContent()).hasSize(10);
            assertThat(firstPage.hasNext()).isTrue();
            assertThat(firstPage.getContent())
                .extracting(Board::getActivityTime)
                .isSortedAccordingTo((t1, t2) -> t2.compareTo(t1));
        }

        @Test
        @DisplayName("두 번째 페이지 조회 - 필터 적용")
        void getAllBoardsSecondPage() {
            // given
            Member testMember = createMember("test@test.com", "tester");
            LocalDateTime baseTime = LocalDateTime.now();
            List<Board> boards = new ArrayList<>();
            // 15개의 게시물 생성 (SOLO, TOP, MID)
            for (int i = 0; i < 15; i++) {
                Board board = createBoard(testMember, GameMode.SOLO, Position.TOP, Position.MID, Mike.AVAILABLE);
                LocalDateTime createdAt = baseTime.minusHours(i).minusMinutes(i).minusSeconds(i);
                ReflectionTestUtils.setField(board, "createdAt", createdAt);
                ReflectionTestUtils.setField(board, "activityTime", createdAt);
                if (i % 3 == 0) {
                    LocalDateTime bumpTime = baseTime.plusMinutes(i).plusSeconds(i);
                    board.bump(bumpTime);
                    ReflectionTestUtils.setField(board, "activityTime", bumpTime);
                }
                em.persist(board);
                boards.add(board);
            }
            em.flush();
            em.clear();

            Pageable pageable = PageRequest.of(0, 10);
            List<Position> positionList = List.of(Position.TOP);  // 주 포지션 또는 부 포지션이 TOP인 게시물
            // 첫 페이지 조회
            Slice<Board> firstPage = boardRepository.findAllBoardsWithCursor(null, null, GameMode.SOLO, null, positionList, null, pageable);
            assertThat(firstPage.getContent()).hasSize(10);
            assertThat(firstPage.hasNext()).isTrue();
            // 두 번째 페이지 조회
            Board lastBoard = firstPage.getContent().get(firstPage.getContent().size() - 1);
            LocalDateTime cursorTime = lastBoard.getActivityTime();
            Long cursorId = lastBoard.getId();
            Slice<Board> result = boardRepository.findAllBoardsWithCursor(cursorTime, cursorId, GameMode.SOLO, null, positionList, null, pageable);

            // then
            assertThat(result.getContent()).hasSize(5);  // SOLO 게시물 중 남은 5개
            assertThat(result.hasNext()).isFalse();
            assertThat(result.getContent())
                .extracting(Board::getActivityTime)
                .isSortedAccordingTo((t1, t2) -> t2.compareTo(t1));
        }
    }
} 