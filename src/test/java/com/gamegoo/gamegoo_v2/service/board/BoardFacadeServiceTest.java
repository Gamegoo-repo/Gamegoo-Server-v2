package com.gamegoo.gamegoo_v2.service.board;

import com.gamegoo.gamegoo_v2.account.member.domain.LoginType;
import com.gamegoo.gamegoo_v2.account.member.domain.Member;
import com.gamegoo.gamegoo_v2.account.member.domain.Mike;
import com.gamegoo.gamegoo_v2.account.member.domain.Position;
import com.gamegoo.gamegoo_v2.account.auth.domain.Role;
import com.gamegoo.gamegoo_v2.account.member.domain.Tier;
import com.gamegoo.gamegoo_v2.account.member.service.MemberService;
import com.gamegoo.gamegoo_v2.content.board.domain.Board;
import com.gamegoo.gamegoo_v2.content.board.dto.request.BoardInsertRequest;
import com.gamegoo.gamegoo_v2.content.board.dto.request.GuestBoardInsertRequest;
import com.gamegoo.gamegoo_v2.content.board.dto.response.BoardByIdResponseForMember;
import com.gamegoo.gamegoo_v2.content.board.dto.response.BoardCursorResponse;
import com.gamegoo.gamegoo_v2.content.board.dto.response.BoardInsertResponse;
import com.gamegoo.gamegoo_v2.content.board.dto.response.BoardListResponse;
import com.gamegoo.gamegoo_v2.content.board.dto.response.BoardResponse;
import com.gamegoo.gamegoo_v2.content.board.service.BoardFacadeService;
import com.gamegoo.gamegoo_v2.content.board.service.BoardGameStyleService;
import com.gamegoo.gamegoo_v2.content.board.service.BoardService;
import com.gamegoo.gamegoo_v2.content.board.service.ProfanityCheckService;
import com.gamegoo.gamegoo_v2.external.riot.service.RiotAuthService;
import com.gamegoo.gamegoo_v2.external.riot.service.RiotInfoService;
import com.gamegoo.gamegoo_v2.external.riot.service.RiotRecordService;
import com.gamegoo.gamegoo_v2.core.exception.BoardException;
import com.gamegoo.gamegoo_v2.core.exception.common.ErrorCode;
import com.gamegoo.gamegoo_v2.matching.domain.GameMode;
import com.gamegoo.gamegoo_v2.social.block.service.BlockService;
import com.gamegoo.gamegoo_v2.social.friend.service.FriendService;
import com.gamegoo.gamegoo_v2.social.manner.service.MannerService;
import com.gamegoo.gamegoo_v2.core.common.validator.BanValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.SliceImpl;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@ExtendWith(MockitoExtension.class)
@org.springframework.transaction.annotation.Transactional
class BoardFacadeServiceTest {

    @Mock
    private BoardService boardService;

    @Mock
    private FriendService friendService;

    @Mock
    private BlockService blockService;

    @Mock
    private MannerService mannerService;

    @Mock
    private BoardGameStyleService boardGameStyleService;

    @Mock
    private ProfanityCheckService profanityCheckService;

    @Mock
    private MemberService memberService;

    @Mock
    private RiotAuthService riotAuthService;

    @Mock
    private RiotInfoService riotInfoService;

    @Mock
    private RiotRecordService riotRecordService;

    @Mock
    private BanValidator banValidator;

    @InjectMocks
    private BoardFacadeService boardFacadeService;

    @PersistenceContext
    EntityManager em;

    @BeforeEach
    void cleanUp() {
        if (em != null) {
            em.flush();
            em.clear();
        }
    }

    private Member createMember(String email, String gameName) {
        Member member = Member.createForGeneral(
                email,
                "password",
                LoginType.GENERAL,
                gameName,
                "TAG",
                Tier.GOLD,
                4,
                55.0,
                Tier.GOLD,
                4,
                55.0,
                100,
                100,
                true
        );
        ReflectionTestUtils.setField(member, "id", 1L);
        return member;
    }

    private Board createBoard(Member member, GameMode gameMode, Position position1, Position position2, Mike mike) {
        Board board = Board.create(
                member,
                gameMode,
                position1,
                position2,
                Arrays.asList(Position.TOP, Position.MID),
                mike,
                "test content"
        );
        ReflectionTestUtils.setField(board, "id", 1L);
        return board;
    }

    @Test
    @DisplayName("게시글 상세 조회 시 매너 정보가 정상적으로 반환되는지 테스트")
    void getBoardByIdForMember_ShouldReturnMannerInfo() {
        // given
        Member poster = Member.createForGeneral(
                "test@email.com",
                "password",
                LoginType.GENERAL,
                "testGameName",
                "testTag",
                Tier.GOLD,
                1,
                60.0,
                Tier.PLATINUM,
                2,
                55.0,
                100,
                50,
                true
        );
        ReflectionTestUtils.setField(poster, "id", 1L);
        ReflectionTestUtils.setField(poster, "mannerLevel", 3);
        ReflectionTestUtils.setField(poster, "mannerRank", 0.85);

        Member viewer = Member.createForGeneral(
                "viewer@email.com",
                "password",
                LoginType.GENERAL,
                "viewerGameName",
                "viewerTag",
                Tier.SILVER,
                1,
                55.0,
                Tier.GOLD,
                2,
                50.0,
                80,
                40,
                true
        );
        ReflectionTestUtils.setField(viewer, "id", 2L);

        Board board = Board.create(
                poster,
                GameMode.SOLO,
                Position.TOP,
                Position.JUNGLE,
                Arrays.asList(Position.TOP, Position.MID),
                Mike.AVAILABLE,
                "test content"
        );
        ReflectionTestUtils.setField(board, "id", 1L);

        // when
        when(boardService.findBoard(1L)).thenReturn(board);
        when(blockService.isBlocked(viewer, poster)).thenReturn(false);
        when(friendService.isFriend(viewer, poster)).thenReturn(false);
        when(friendService.getFriendRequestMemberId(viewer, poster)).thenReturn(null);
        when(mannerService.countMannerRatingByMember(poster, true)).thenReturn(10);

        BoardByIdResponseForMember response = boardFacadeService.getBoardByIdForMember(1L, viewer);

        // then
        assertThat(response.getBoardId()).isEqualTo(1L);
        assertThat(response.getMemberId()).isEqualTo(1L);
        assertThat(response.getMannerLevel()).isEqualTo(3);
        assertThat(response.getMannerRank()).isEqualTo(0.85);
        assertThat(response.getMannerRatingCount()).isEqualTo(10);
    }

    @Test
    @DisplayName("게시물 목록 조회 시 주 포지션과 부 포지션으로 필터링이 정상적으로 동작하는지 테스트")
    void getBoardList_ShouldFilterByMainPAndSubP() {
        // given
        GameMode gameMode = GameMode.SOLO;
        Tier tier = Tier.GOLD;
        Position mainP = Position.TOP;
        Position subP = Position.JUNGLE;
        Mike mike = Mike.AVAILABLE;
        int pageIdx = 1;

        Member member = Member.builder()
                .email("test@test.com")
                .password("password")
                .loginType(LoginType.GENERAL)
                .gameName("testGameName")
                .tag("testTag")
                .soloTier(tier)
                .soloRank(1)
                .soloWinRate(60.0)
                .freeTier(Tier.PLATINUM)
                .freeRank(2)
                .freeWinRate(55.0)
                .build();
        ReflectionTestUtils.setField(member, "id", 1L);

        Board board = Board.builder()
                .gameMode(gameMode)
                .mainP(mainP)
                .subP(subP)
                .wantP(List.of(Position.TOP))
                .mike(mike)
                .member(member)
                .content("test content")
                .deleted(false)
                .build();
        ReflectionTestUtils.setField(board, "id", 1L);
        ReflectionTestUtils.setField(board, "createdAt", LocalDateTime.now());

        Page<Board> expectedPage = new PageImpl<>(List.of(board));

        when(boardService.getBoardsWithPagination(gameMode, tier, mainP, subP, mike, pageIdx))
                .thenReturn(expectedPage);

        // when
        BoardResponse response = boardFacadeService.getBoardList(null, gameMode, tier, mainP, subP, mike, pageIdx);

        // then
        assertThat(response.getBoards()).hasSize(1);
        assertThat(response.getBoards().get(0).getMainP()).isEqualTo(mainP);
        assertThat(response.getBoards().get(0).getSubP()).isEqualTo(subP);
        assertThat(response.getBoards().get(0).getContents()).isEqualTo("test content");
        assertThat(response.getBoards().get(0).getFreeTier()).isEqualTo(Tier.PLATINUM);
        assertThat(response.getBoards().get(0).getFreeRank()).isEqualTo(2);
        assertThat(response.getBoards().get(0).getSoloTier()).isEqualTo(Tier.GOLD);
        assertThat(response.getBoards().get(0).getSoloRank()).isEqualTo(1);
    }

    @Test
    @DisplayName("게시물 목록 조회 시 부 포지션이 null일 경우 ANY로 처리되는지 테스트")
    void getBoardList_ShouldHandleNullSubP() {
        // given
        GameMode gameMode = GameMode.SOLO;
        Tier tier = Tier.GOLD;
        Position mainP = Position.TOP;
        Position subP = null;
        Mike mike = Mike.AVAILABLE;
        int pageIdx = 1;

        Member member = Member.builder()
                .email("test@test.com")
                .password("password")
                .loginType(LoginType.GENERAL)
                .gameName("testGameName")
                .tag("testTag")
                .soloTier(tier)
                .soloRank(1)
                .soloWinRate(60.0)
                .freeTier(Tier.PLATINUM)
                .freeRank(2)
                .freeWinRate(55.0)
                .build();
        ReflectionTestUtils.setField(member, "id", 1L);

        Board board = Board.builder()
                .gameMode(gameMode)
                .mainP(mainP)
                .subP(Position.ANY)
                .wantP(List.of(Position.TOP))
                .mike(mike)
                .member(member)
                .content("test content")
                .deleted(false)
                .build();
        ReflectionTestUtils.setField(board, "id", 1L);
        ReflectionTestUtils.setField(board, "createdAt", LocalDateTime.now());

        Page<Board> expectedPage = new PageImpl<>(List.of(board));

        when(boardService.getBoardsWithPagination(gameMode, tier, mainP, Position.ANY, mike, pageIdx))
                .thenReturn(expectedPage);

        // when
        BoardResponse response = boardFacadeService.getBoardList(null, gameMode, tier, mainP, subP, mike, pageIdx);

        // then
        assertThat(response.getBoards()).hasSize(1);
        assertThat(response.getBoards().get(0).getSubP()).isEqualTo(Position.ANY);
    }

    @Test
    @DisplayName("게시물 목록 조회 시 메인 포지션이 ANY일 경우 모든 포지션의 게시물이 조회되는지 테스트")
    void getBoardList_ShouldReturnAllPositionsWhenMainPIsANY() {
        // given
        GameMode gameMode = GameMode.SOLO;
        Tier tier = Tier.GOLD;
        Position mainP = Position.ANY;
        Position subP = Position.JUNGLE;
        Mike mike = Mike.AVAILABLE;
        int pageIdx = 1;

        Member member = Member.builder()
                .email("test@test.com")
                .password("password")
                .loginType(LoginType.GENERAL)
                .gameName("testGameName")
                .tag("testTag")
                .soloTier(tier)
                .soloRank(1)
                .soloWinRate(60.0)
                .freeTier(Tier.PLATINUM)
                .freeRank(2)
                .freeWinRate(55.0)
                .build();
        ReflectionTestUtils.setField(member, "id", 1L);

        Board board1 = Board.builder()
                .gameMode(gameMode)
                .mainP(Position.TOP)
                .subP(subP)
                .wantP(List.of(Position.TOP))
                .mike(mike)
                .member(member)
                .content("test content 1")
                .deleted(false)
                .build();
        ReflectionTestUtils.setField(board1, "id", 1L);
        ReflectionTestUtils.setField(board1, "createdAt", LocalDateTime.now());

        Board board2 = Board.builder()
                .gameMode(gameMode)
                .mainP(Position.MID)
                .subP(subP)
                .wantP(List.of(Position.MID))
                .mike(mike)
                .member(member)
                .content("test content 2")
                .deleted(false)
                .build();
        ReflectionTestUtils.setField(board2, "id", 2L);
        ReflectionTestUtils.setField(board2, "createdAt", LocalDateTime.now());

        Page<Board> expectedPage = new PageImpl<>(List.of(board1, board2));

        when(boardService.getBoardsWithPagination(gameMode, tier, mainP, subP, mike, pageIdx))
                .thenReturn(expectedPage);

        // when
        BoardResponse response = boardFacadeService.getBoardList(null, gameMode, tier, mainP, subP, mike, pageIdx);

        // then
        assertThat(response.getBoards()).hasSize(2);
        assertThat(response.getBoards().stream().map(BoardListResponse::getMainP))
                .containsExactlyInAnyOrder(Position.TOP, Position.MID);
    }

    @Test
    @DisplayName("게시물 목록 조회 시 메인 포지션이 미드와 서폿일 경우 두 포지션의 게시물이 모두 조회되는지 테스트")
    void getBoardList_ShouldReturnBothMidAndSupportWhenMainPIsBoth() {
        // given
        GameMode gameMode = GameMode.SOLO;
        Tier tier = Tier.GOLD;
        Position mainP = Position.MID; // 미드와 서폿을 선택
        Position subP = Position.JUNGLE;
        Mike mike = Mike.AVAILABLE;
        int pageIdx = 1;

        Member member = Member.builder()
                .email("test@test.com")
                .password("password")
                .loginType(LoginType.GENERAL)
                .gameName("testGameName")
                .tag("testTag")
                .soloTier(tier)
                .soloRank(1)
                .soloWinRate(60.0)
                .freeTier(Tier.PLATINUM)
                .freeRank(2)
                .freeWinRate(55.0)
                .build();
        ReflectionTestUtils.setField(member, "id", 1L);

        Board midBoard = Board.builder()
                .gameMode(gameMode)
                .mainP(Position.MID)
                .subP(subP)
                .wantP(List.of(Position.MID))
                .mike(mike)
                .member(member)
                .content("test content 1")
                .deleted(false)
                .build();
        ReflectionTestUtils.setField(midBoard, "id", 1L);
        ReflectionTestUtils.setField(midBoard, "createdAt", LocalDateTime.now());

        Board supportBoard = Board.builder()
                .gameMode(gameMode)
                .mainP(Position.SUP)
                .subP(subP)
                .wantP(List.of(Position.SUP))
                .mike(mike)
                .member(member)
                .content("test content 2")
                .deleted(false)
                .build();
        ReflectionTestUtils.setField(supportBoard, "id", 2L);
        ReflectionTestUtils.setField(supportBoard, "createdAt", LocalDateTime.now());

        Page<Board> expectedPage = new PageImpl<>(List.of(midBoard, supportBoard));

        when(boardService.getBoardsWithPagination(gameMode, tier, mainP, subP, mike, pageIdx))
                .thenReturn(expectedPage);

        // when
        BoardResponse response = boardFacadeService.getBoardList(null, gameMode, tier, mainP, subP, mike, pageIdx);

        // then
        assertThat(response.getBoards()).hasSize(2);
        assertThat(response.getBoards().stream().map(BoardListResponse::getMainP))
                .containsExactlyInAnyOrder(Position.MID, Position.SUP);
    }

    @Test
    @DisplayName("게시물 목록 조회 시 메인 포지션이 탑과 정글일 경우 두 포지션의 게시물이 모두 조회되는지 테스트")
    void getBoardList_ShouldReturnBothTopAndJungleWhenMainPIsBoth() {
        // given
        GameMode gameMode = GameMode.SOLO;
        Tier tier = Tier.GOLD;
        Position mainP = Position.TOP; // 탑과 정글을 선택
        Position subP = Position.MID;
        Mike mike = Mike.AVAILABLE;
        int pageIdx = 1;

        Member member = Member.builder()
                .email("test@test.com")
                .password("password")
                .loginType(LoginType.GENERAL)
                .gameName("testGameName")
                .tag("testTag")
                .soloTier(tier)
                .soloRank(1)
                .soloWinRate(60.0)
                .freeTier(Tier.PLATINUM)
                .freeRank(2)
                .freeWinRate(55.0)
                .build();
        ReflectionTestUtils.setField(member, "id", 1L);

        Board topBoard = Board.builder()
                .gameMode(gameMode)
                .mainP(Position.TOP)
                .subP(subP)
                .wantP(List.of(Position.TOP))
                .mike(mike)
                .member(member)
                .content("test content 1")
                .deleted(false)
                .build();
        ReflectionTestUtils.setField(topBoard, "id", 1L);
        ReflectionTestUtils.setField(topBoard, "createdAt", LocalDateTime.now());

        Board jungleBoard = Board.builder()
                .gameMode(gameMode)
                .mainP(Position.JUNGLE)
                .subP(subP)
                .wantP(List.of(Position.JUNGLE))
                .mike(mike)
                .member(member)
                .content("test content 2")
                .deleted(false)
                .build();
        ReflectionTestUtils.setField(jungleBoard, "id", 2L);
        ReflectionTestUtils.setField(jungleBoard, "createdAt", LocalDateTime.now());

        Page<Board> expectedPage = new PageImpl<>(List.of(topBoard, jungleBoard));

        when(boardService.getBoardsWithPagination(gameMode, tier, mainP, subP, mike, pageIdx))
                .thenReturn(expectedPage);

        // when
        BoardResponse response = boardFacadeService.getBoardList(null, gameMode, tier, mainP, subP, mike, pageIdx);

        // then
        assertThat(response.getBoards()).hasSize(2);
        assertThat(response.getBoards().stream().map(BoardListResponse::getMainP))
                .containsExactlyInAnyOrder(Position.TOP, Position.JUNGLE);
    }

    @Test
    @DisplayName("게시물 목록 조회 시 메인 포지션이 원딜과 서폿일 경우 두 포지션의 게시물이 모두 조회되는지 테스트")
    void getBoardList_ShouldReturnBothAdcAndSupportWhenMainPIsBoth() {
        // given
        GameMode gameMode = GameMode.SOLO;
        Tier tier = Tier.GOLD;
        Position mainP = Position.ADC; // 원딜과 서폿을 선택
        Position subP = Position.MID;
        Mike mike = Mike.AVAILABLE;
        int pageIdx = 1;

        Member member = Member.builder()
                .email("test@test.com")
                .password("password")
                .loginType(LoginType.GENERAL)
                .gameName("testGameName")
                .tag("testTag")
                .soloTier(tier)
                .soloRank(1)
                .soloWinRate(60.0)
                .freeTier(Tier.PLATINUM)
                .freeRank(2)
                .freeWinRate(55.0)
                .build();
        ReflectionTestUtils.setField(member, "id", 1L);

        Board adcBoard = Board.builder()
                .gameMode(gameMode)
                .mainP(Position.ADC)
                .subP(subP)
                .wantP(List.of(Position.ADC))
                .mike(mike)
                .member(member)
                .content("test content 1")
                .deleted(false)
                .build();
        ReflectionTestUtils.setField(adcBoard, "id", 1L);
        ReflectionTestUtils.setField(adcBoard, "createdAt", LocalDateTime.now());

        Board supportBoard = Board.builder()
                .gameMode(gameMode)
                .mainP(Position.SUP)
                .subP(subP)
                .wantP(List.of(Position.SUP))
                .mike(mike)
                .member(member)
                .content("test content 2")
                .deleted(false)
                .build();
        ReflectionTestUtils.setField(supportBoard, "id", 2L);
        ReflectionTestUtils.setField(supportBoard, "createdAt", LocalDateTime.now());

        Page<Board> expectedPage = new PageImpl<>(List.of(adcBoard, supportBoard));

        when(boardService.getBoardsWithPagination(gameMode, tier, mainP, subP, mike, pageIdx))
                .thenReturn(expectedPage);

        // when
        BoardResponse response = boardFacadeService.getBoardList(null, gameMode, tier, mainP, subP, mike, pageIdx);

        // then
        assertThat(response.getBoards()).hasSize(2);
        assertThat(response.getBoards().stream().map(BoardListResponse::getMainP))
                .containsExactlyInAnyOrder(Position.ADC, Position.SUP);
    }

    @Test
    @DisplayName("게시물 목록 조회 시 메인 포지션이 탑과 원딜일 경우 두 포지션의 게시물이 모두 조회되는지 테스트")
    void getBoardList_ShouldReturnBothTopAndAdcWhenMainPIsBoth() {
        // given
        GameMode gameMode = GameMode.SOLO;
        Tier tier = Tier.GOLD;
        Position mainP = Position.TOP; // 탑과 원딜을 선택
        Position subP = Position.MID;
        Mike mike = Mike.AVAILABLE;
        int pageIdx = 1;

        Member member = Member.builder()
                .email("test@test.com")
                .password("password")
                .loginType(LoginType.GENERAL)
                .gameName("testGameName")
                .tag("testTag")
                .soloTier(tier)
                .soloRank(1)
                .soloWinRate(60.0)
                .freeTier(Tier.PLATINUM)
                .freeRank(2)
                .freeWinRate(55.0)
                .build();
        ReflectionTestUtils.setField(member, "id", 1L);

        Board topBoard = Board.builder()
                .gameMode(gameMode)
                .mainP(Position.TOP)
                .subP(subP)
                .wantP(List.of(Position.TOP))
                .mike(mike)
                .member(member)
                .content("test content 1")
                .deleted(false)
                .build();
        ReflectionTestUtils.setField(topBoard, "id", 1L);
        ReflectionTestUtils.setField(topBoard, "createdAt", LocalDateTime.now());

        Board adcBoard = Board.builder()
                .gameMode(gameMode)
                .mainP(Position.ADC)
                .subP(subP)
                .wantP(List.of(Position.ADC))
                .mike(mike)
                .member(member)
                .content("test content 2")
                .deleted(false)
                .build();
        ReflectionTestUtils.setField(adcBoard, "id", 2L);
        ReflectionTestUtils.setField(adcBoard, "createdAt", LocalDateTime.now());

        Page<Board> expectedPage = new PageImpl<>(List.of(topBoard, adcBoard));

        when(boardService.getBoardsWithPagination(gameMode, tier, mainP, subP, mike, pageIdx))
                .thenReturn(expectedPage);

        // when
        BoardResponse response = boardFacadeService.getBoardList(null, gameMode, tier, mainP, subP, mike, pageIdx);

        // then
        assertThat(response.getBoards()).hasSize(2);
        assertThat(response.getBoards().stream().map(BoardListResponse::getMainP))
                .containsExactlyInAnyOrder(Position.TOP, Position.ADC);
    }

    @Nested
    @DisplayName("전체 게시물 커서 기반 조회")
    class GetAllBoardsWithCursorTest {

        @Test
        @DisplayName("첫 페이지 조회 - 필터 없음")
        void getAllBoardsFirstPage() {
            // given
            LocalDateTime baseTime = LocalDateTime.now();
            List<Board> boards = new ArrayList<>();
            Member member = createMember("test@test.com", "testUser");

            for (int i = 0; i < 10; i++) {
                Board board = createBoard(member, GameMode.SOLO, Position.TOP, Position.JUNGLE, Mike.AVAILABLE);
                ReflectionTestUtils.setField(board, "createdAt", baseTime.minusMinutes(i));
                if (i % 2 == 0) {
                    board.bump(baseTime.plusMinutes(i));
                }
                boards.add(board);
            }

            when(boardService.getAllBoardsWithCursor(
                eq(null), eq(null), eq(null), eq(null), eq(null), eq(null), eq(null)
            )).thenReturn(new SliceImpl<>(boards, PageRequest.of(0, 10), false));

            // when
            BoardCursorResponse response = boardFacadeService.getAllBoardsWithCursor(null, null, null, null, null, null, null, null);

            // then
            assertThat(response.getBoards()).hasSize(10);
            assertThat(response.isHasNext()).isFalse();
            assertThat(response.getBoards()).isSortedAccordingTo(
                (b1, b2) -> b2.getCreatedAt().compareTo(b1.getCreatedAt())
            );
        }

        @Test
        @DisplayName("두 번째 페이지 조회 - 필터 적용")
        void getAllBoardsSecondPage() {
            // given
            LocalDateTime baseTime = LocalDateTime.now();
            List<Board> boards = new ArrayList<>();
            Member member = createMember("test@test.com", "testUser");

            // 15개의 게시물 생성 (SOLO, TOP, JUNGLE, TIER.GOLD)
            for (int i = 0; i < 15; i++) {
                Board board = createBoard(member, GameMode.SOLO, Position.TOP, Position.JUNGLE, Mike.AVAILABLE);
                LocalDateTime createdAt = baseTime.minusMinutes(i).minusSeconds(i);
                ReflectionTestUtils.setField(board, "createdAt", createdAt);
                if (i % 3 == 0) {
                    board.bump(baseTime.plusMinutes(i).plusSeconds(i));
                }
                boards.add(board);
            }

            when(boardService.getAllBoardsWithCursor(
                eq(null), eq(null), eq(GameMode.SOLO), eq(Tier.GOLD), eq(Position.TOP), eq(Position.JUNGLE), eq(null)
            )).thenReturn(new SliceImpl<>(boards.subList(0, 10), PageRequest.of(0, 10), true));

            // 첫 페이지 조회
            BoardCursorResponse response = boardFacadeService.getAllBoardsWithCursor(null, null, GameMode.SOLO, Tier.GOLD, Position.TOP, Position.JUNGLE, null, null);
            assertThat(response.getBoards()).hasSize(10);
            assertThat(response.isHasNext()).isTrue();

            // 두 번째 페이지 조회
            Board lastBoard = boards.get(9);
            LocalDateTime lastActivityTime = lastBoard.getBumpTime() != null ? lastBoard.getBumpTime() : lastBoard.getCreatedAt();
            Long lastId = lastBoard.getId();
            when(boardService.getAllBoardsWithCursor(
                eq(lastActivityTime), eq(lastId), eq(GameMode.SOLO), eq(Tier.GOLD), eq(Position.TOP), eq(Position.JUNGLE), eq(null)
            )).thenReturn(new SliceImpl<>(boards.subList(10, 15), PageRequest.of(0, 10), false));
            BoardCursorResponse response2 = boardFacadeService.getAllBoardsWithCursor(lastActivityTime, lastId, GameMode.SOLO, Tier.GOLD, Position.TOP, Position.JUNGLE, null, null);

            // then
            assertThat(response2.getBoards()).hasSize(5);
            assertThat(response2.isHasNext()).isFalse();
            assertThat(response2.getBoards()).allSatisfy(board -> {
                assertThat(board.getGameMode()).isEqualTo(GameMode.SOLO);
                assertThat(board.getMainP()).isEqualTo(Position.TOP);
                assertThat(board.getSubP()).isEqualTo(Position.JUNGLE);
            });
        }
    }

    @Test
    @DisplayName("내가 작성한 게시글 커서 기반 조회가 정상 동작하는지 테스트")
    void getMyBoardCursorList() {
        // given
        Member member = createMember("test@test.com", "testUser");
        LocalDateTime baseTime = LocalDateTime.now();
        List<Board> boards = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            Board board = createBoard(member, GameMode.SOLO, Position.TOP, Position.JUNGLE, Mike.AVAILABLE);
            ReflectionTestUtils.setField(board, "createdAt", baseTime.minusMinutes(i));
            if (i % 2 == 0) {
                board.bump(baseTime.plusMinutes(i));
            }
            boards.add(board);
        }

        when(boardService.getMyBoards(eq(member.getId()), eq(null)))
            .thenReturn(new SliceImpl<>(boards, PageRequest.of(0, 10), false));

        // when
        var response = boardFacadeService.getMyBoardCursorList(member, null);

        // then
        assertThat(response.getMyBoards()).hasSize(10);
        assertThat(response.isHasNext()).isFalse();
        assertThat(response.getMyBoards()).isSortedAccordingTo(
            (b1, b2) -> b2.getCreatedAt().compareTo(b1.getCreatedAt())
        );
    }

    @Nested
    @DisplayName("게스트 게시글 작성")
    class CreateGuestBoardTest {

        private BoardInsertRequest createBoardInsertRequest() {
            BoardInsertRequest request = new BoardInsertRequest();
            ReflectionTestUtils.setField(request, "gameMode", GameMode.SOLO);
            ReflectionTestUtils.setField(request, "mainP", Position.TOP);
            ReflectionTestUtils.setField(request, "subP", Position.JUNGLE);
            ReflectionTestUtils.setField(request, "wantP", Arrays.asList(Position.TOP, Position.MID));
            ReflectionTestUtils.setField(request, "mike", Mike.AVAILABLE);
            ReflectionTestUtils.setField(request, "contents", "게스트 게시글 내용");
            ReflectionTestUtils.setField(request, "gameStyles", Arrays.asList(1L, 2L));
            return request;
        }

        private GuestBoardInsertRequest createGuestBoardInsertRequest(String gameName, String tag, String password) {
            GuestBoardInsertRequest request = new GuestBoardInsertRequest();
            ReflectionTestUtils.setField(request, "gameMode", GameMode.SOLO);
            ReflectionTestUtils.setField(request, "mainP", Position.TOP);
            ReflectionTestUtils.setField(request, "subP", Position.JUNGLE);
            ReflectionTestUtils.setField(request, "wantP", Arrays.asList(Position.TOP, Position.MID));
            ReflectionTestUtils.setField(request, "mike", Mike.AVAILABLE);
            ReflectionTestUtils.setField(request, "contents", "게스트 게시글 내용");
            ReflectionTestUtils.setField(request, "gameStyles", Arrays.asList(1L, 2L));
            ReflectionTestUtils.setField(request, "gameName", gameName);
            ReflectionTestUtils.setField(request, "tag", tag);
            ReflectionTestUtils.setField(request, "password", password);
            return request;
        }

        @Test
        @DisplayName("게스트 게시글 작성 성공")
        void createGuestBoard_Success() {
            // given
            String gameName = "testGuestUser";
            String tag = "KR1";
            String password = "testpass123";
            GuestBoardInsertRequest request = createGuestBoardInsertRequest(gameName, tag, password);

            Member tmpMember = Member.createForTmp(
                    gameName, tag, Tier.GOLD, 4, 55.0,
                    Tier.SILVER, 2, 45.0, 100, 50
            );
            tmpMember.updateRole(Role.TMP);
            ReflectionTestUtils.setField(tmpMember, "id", 1L);

            Board guestBoard = Board.createForGuest(
                    tmpMember, request.getGameMode(), request.getMainP(), request.getSubP(),
                    request.getWantP(), request.getMike(), request.getContents(), password
            );
            ReflectionTestUtils.setField(guestBoard, "id", 1L);

            // when
            when(riotAuthService.getPuuid(gameName, tag)).thenReturn("test-puuid");
            when(riotInfoService.getTierWinrateRank("test-puuid")).thenReturn(null);
            when(riotRecordService.getRecent30GameStats(gameName, "test-puuid")).thenReturn(null);
            when(memberService.getOrCreateTmpMember(eq(gameName), eq(tag), any())).thenReturn(tmpMember);
            when(riotRecordService.getPreferChampionfromMatch(gameName, "test-puuid")).thenReturn(null);
            doNothing().when(profanityCheckService).validateProfanity(request.getContents());
            when(boardService.createAndSaveGuestBoard(request, tmpMember, password)).thenReturn(guestBoard);
            doNothing().when(boardGameStyleService).mapGameStylesToBoard(guestBoard, request.getGameStyles());

            BoardInsertResponse response = boardFacadeService.createGuestBoard(request, gameName, tag);

            // then
            assertThat(response.getBoardId()).isEqualTo(1L);
            assertThat(response.getMemberId()).isEqualTo(null);
            assertThat(response.getGameName()).isEqualTo(gameName);
            assertThat(response.getTag()).isEqualTo(tag);
            verify(riotAuthService).getPuuid(gameName, tag);
            verify(memberService).getOrCreateTmpMember(eq(gameName), eq(tag), any());
            verify(profanityCheckService).validateProfanity(request.getContents());
            verify(boardService).createAndSaveGuestBoard(eq(request), any(Member.class), eq(password));
            verify(boardGameStyleService).mapGameStylesToBoard(guestBoard, request.getGameStyles());
        }

        @Test
        @DisplayName("게스트 게시글 작성 실패 - 기존 회원 존재")
        void createGuestBoard_FailWhenMemberExists() {
            // given
            String gameName = "existingUser";
            String tag = "KR1";
            String password = "testpass123";
            GuestBoardInsertRequest request = createGuestBoardInsertRequest(gameName, tag, password);

            // when
            when(riotAuthService.getPuuid(gameName, tag)).thenReturn("test-puuid");
            when(riotInfoService.getTierWinrateRank("test-puuid")).thenReturn(null);
            when(riotRecordService.getRecent30GameStats(gameName, "test-puuid")).thenReturn(null);
            when(memberService.getOrCreateTmpMember(eq(gameName), eq(tag), any()))
                    .thenThrow(new BoardException(ErrorCode.MEMBER_ALREADY_EXISTS));

            // then
            assertThatThrownBy(() -> boardFacadeService.createGuestBoard(request, gameName, tag))
                    .isInstanceOf(BoardException.class)
                    .hasFieldOrPropertyWithValue("code", ErrorCode.MEMBER_ALREADY_EXISTS.getCode());

            verify(riotAuthService).getPuuid(gameName, tag);
            verify(memberService).getOrCreateTmpMember(eq(gameName), eq(tag), any());
            verify(profanityCheckService, never()).validateProfanity(anyString());
            verify(boardService, never()).createAndSaveGuestBoard(any(), any(Member.class), anyString());
            verify(boardGameStyleService, never()).mapGameStylesToBoard(any(), any());
        }

        @Test
        @DisplayName("게스트 게시글 작성 실패 - 욕설 포함")
        void createGuestBoard_FailWhenContainsProfanity() {
            // given
            String gameName = "testGuestUser";
            String tag = "KR1";
            String password = "testpass123";
            GuestBoardInsertRequest request = createGuestBoardInsertRequest(gameName, tag, password);

            Member tmpMember = Member.createForTmp(
                    gameName, tag, Tier.GOLD, 4, 55.0,
                    Tier.SILVER, 2, 45.0, 100, 50
            );
            tmpMember.updateRole(Role.TMP);
            ReflectionTestUtils.setField(tmpMember, "id", 1L);

            // when
            when(riotAuthService.getPuuid(gameName, tag)).thenReturn("test-puuid");
            when(riotInfoService.getTierWinrateRank("test-puuid")).thenReturn(null);
            when(riotRecordService.getRecent30GameStats(gameName, "test-puuid")).thenReturn(null);
            when(memberService.getOrCreateTmpMember(eq(gameName), eq(tag), any())).thenReturn(tmpMember);
            when(riotRecordService.getPreferChampionfromMatch(gameName, "test-puuid")).thenReturn(null);
            doThrow(new BoardException(ErrorCode.BOARD_FORBIDDEN_WORD))
                    .when(profanityCheckService).validateProfanity(request.getContents());

            // then
            assertThatThrownBy(() -> boardFacadeService.createGuestBoard(request, gameName, tag))
                    .isInstanceOf(BoardException.class)
                    .hasFieldOrPropertyWithValue("code", ErrorCode.BOARD_FORBIDDEN_WORD.getCode());

            verify(riotAuthService).getPuuid(gameName, tag);
            verify(memberService).getOrCreateTmpMember(eq(gameName), eq(tag), any());
            verify(profanityCheckService).validateProfanity(request.getContents());
            verify(boardService, never()).createAndSaveGuestBoard(any(), any(Member.class), anyString());
            verify(boardGameStyleService, never()).mapGameStylesToBoard(any(), any());
        }
    }

    @Nested
    @DisplayName("최신글 자동 끌올 테스트")
    class BumpLatestBoardTest {

        private Member member;
        private Board latestBoard;

        @BeforeEach
        void setUp() {
            member = createMember("test@test.com", "testUser");
            latestBoard = createBoard(member, GameMode.SOLO, Position.TOP, Position.MID, Mike.AVAILABLE);
            ReflectionTestUtils.setField(latestBoard, "id", 1L);
        }

        @Test
        @DisplayName("최신글을 성공적으로 끌올한다")
        void bumpLatestBoard_Success() {
            // given
            LocalDateTime bumpTime = LocalDateTime.now();
            when(boardService.findLatestBoardByMember(member.getId())).thenReturn(latestBoard);
            when(boardService.bumpBoard(latestBoard.getId(), member.getId())).thenReturn(latestBoard);
            ReflectionTestUtils.setField(latestBoard, "bumpTime", bumpTime);

            // when
            var response = boardFacadeService.bumpLatestBoard(member);

            // then
            assertThat(response.getBoardId()).isEqualTo(1L);
            assertThat(response.getBumpTime()).isEqualTo(bumpTime);
            verify(boardService).findLatestBoardByMember(member.getId());
            verify(boardService).bumpBoard(latestBoard.getId(), member.getId());
        }

        @Test
        @DisplayName("작성한 게시글이 없으면 예외가 발생한다")
        void bumpLatestBoard_NoBoardFound() {
            // given
            when(boardService.findLatestBoardByMember(member.getId()))
                    .thenThrow(new BoardException(ErrorCode.BOARD_NOT_FOUND));

            // when & then
            assertThatThrownBy(() -> boardFacadeService.bumpLatestBoard(member))
                    .isInstanceOf(BoardException.class)
                    .hasFieldOrPropertyWithValue("code", ErrorCode.BOARD_NOT_FOUND.getCode());

            verify(boardService).findLatestBoardByMember(member.getId());
            verify(boardService, never()).bumpBoard(anyLong(), anyLong());
        }

        @Test
        @DisplayName("5분 이내에 끌올하면 예외가 발생한다")
        void bumpLatestBoard_TimeLimitError() {
            // given
            when(boardService.findLatestBoardByMember(member.getId())).thenReturn(latestBoard);
            when(boardService.bumpBoard(latestBoard.getId(), member.getId()))
                    .thenThrow(new BoardException(ErrorCode.BUMP_TIME_LIMIT));

            // when & then
            assertThatThrownBy(() -> boardFacadeService.bumpLatestBoard(member))
                    .isInstanceOf(BoardException.class)
                    .hasFieldOrPropertyWithValue("code", ErrorCode.BUMP_TIME_LIMIT.getCode());

            verify(boardService).findLatestBoardByMember(member.getId());
            verify(boardService).bumpBoard(latestBoard.getId(), member.getId());
        }
    }

}
