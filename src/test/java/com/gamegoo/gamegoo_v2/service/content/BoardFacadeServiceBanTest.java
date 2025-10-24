package com.gamegoo.gamegoo_v2.service.content;

import com.gamegoo.gamegoo_v2.account.member.domain.BanType;
import com.gamegoo.gamegoo_v2.account.member.domain.LoginType;
import com.gamegoo.gamegoo_v2.account.member.domain.Member;
import com.gamegoo.gamegoo_v2.account.member.domain.Mike;
import com.gamegoo.gamegoo_v2.account.member.domain.Position;
import com.gamegoo.gamegoo_v2.account.member.domain.Tier;
import com.gamegoo.gamegoo_v2.content.board.domain.Board;
import com.gamegoo.gamegoo_v2.content.board.dto.request.BoardInsertRequest;
import com.gamegoo.gamegoo_v2.content.board.dto.request.BoardUpdateRequest;
import com.gamegoo.gamegoo_v2.content.board.service.BoardFacadeService;
import com.gamegoo.gamegoo_v2.content.board.service.BoardGameStyleService;
import com.gamegoo.gamegoo_v2.content.board.service.BoardService;
import com.gamegoo.gamegoo_v2.content.board.service.ProfanityCheckService;
import com.gamegoo.gamegoo_v2.core.common.validator.BanValidator;
import com.gamegoo.gamegoo_v2.core.exception.MemberException;
import com.gamegoo.gamegoo_v2.core.exception.common.ErrorCode;
import com.gamegoo.gamegoo_v2.matching.domain.GameMode;
import com.gamegoo.gamegoo_v2.social.block.service.BlockService;
import com.gamegoo.gamegoo_v2.social.friend.service.FriendService;
import com.gamegoo.gamegoo_v2.social.manner.service.MannerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("BoardFacadeService 제재 검증 테스트")
class BoardFacadeServiceBanTest {

    @Mock
    private BoardService boardService;

    @Mock
    private BoardGameStyleService boardGameStyleService;

    @Mock
    private ProfanityCheckService profanityCheckService;

    @Mock
    private BanValidator banValidator;

    @InjectMocks
    private BoardFacadeService boardFacadeService;

    private Member bannedMember;
    private Member normalMember;
    private Board board;

    @BeforeEach
    void setUp() {
        bannedMember = createMember(BanType.BAN_1D, LocalDateTime.now().plusDays(1));
        ReflectionTestUtils.setField(bannedMember, "id", 1L);

        normalMember = createMember(BanType.NONE, null);
        ReflectionTestUtils.setField(normalMember, "id", 2L);

        board = Board.builder()
                .member(normalMember)
                .gameMode(GameMode.SOLO)
                .mainP(Position.TOP)
                .subP(Position.JUNGLE)
                .wantP(List.of(Position.ANY))
                .mike(Mike.AVAILABLE)
                .content("test content")
                .build();
        ReflectionTestUtils.setField(board, "id", 1L);
    }

    @Nested
    @DisplayName("게시글 생성 제재 검증")
    class CreateBoardBanValidationTest {

        @Test
        @DisplayName("제재된 사용자가 게시글을 생성하려고 하면 BanValidator에서 예외가 발생한다")
        void banned_user_cannot_create_board() {
            // given
            BoardInsertRequest request = createBoardInsertRequest();

            doThrow(new MemberException(ErrorCode.MEMBER_BANNED_FROM_POSTING))
                    .when(banValidator).throwIfBannedFromPosting(bannedMember);

            // when & then
            assertThatThrownBy(() -> boardFacadeService.createBoard(request, bannedMember))
                    .isInstanceOf(MemberException.class)
                    .hasFieldOrPropertyWithValue("code", ErrorCode.MEMBER_BANNED_FROM_POSTING.getCode());

            // 제재 검증에서 실패하면 이후 로직은 실행되지 않아야 함
            verify(banValidator).throwIfBannedFromPosting(bannedMember);
            verify(profanityCheckService, never()).validateProfanity(anyString());
            verify(boardService, never()).createAndSaveBoard(any(), any());
            verify(boardGameStyleService, never()).mapGameStylesToBoard(any(), anyList());
        }

        @Test
        @DisplayName("제재되지 않은 사용자는 게시글을 정상적으로 생성할 수 있다")
        void normal_user_can_create_board() {
            // given
            BoardInsertRequest request = createBoardInsertRequest();

            doNothing().when(banValidator).throwIfBannedFromPosting(normalMember);
            doNothing().when(profanityCheckService).validateProfanity(anyString());
            when(boardService.createAndSaveBoard(any(), any())).thenReturn(board);
            doNothing().when(boardGameStyleService).mapGameStylesToBoard(any(), anyList());

            // when
            boardFacadeService.createBoard(request, normalMember);

            // then
            verify(banValidator).throwIfBannedFromPosting(normalMember);
            // 제재 검증 통과 후 정상 로직이 실행되어야 함
            verify(profanityCheckService).validateProfanity(anyString());
            verify(boardService).createAndSaveBoard(any(), any());
            verify(boardGameStyleService).mapGameStylesToBoard(any(), anyList());
        }

        @Test
        @DisplayName("영구 제재된 사용자는 게시글을 생성할 수 없다")
        void permanently_banned_user_cannot_create_board() {
            // given
            Member permanentBannedMember = createMember(BanType.PERMANENT, null);
            BoardInsertRequest request = createBoardInsertRequest();

            doThrow(new MemberException(ErrorCode.MEMBER_BANNED_FROM_POSTING))
                    .when(banValidator).throwIfBannedFromPosting(permanentBannedMember);

            // when & then
            assertThatThrownBy(() -> boardFacadeService.createBoard(request, permanentBannedMember))
                    .isInstanceOf(MemberException.class)
                    .hasFieldOrPropertyWithValue("code", ErrorCode.MEMBER_BANNED_FROM_POSTING.getCode());

            verify(banValidator).throwIfBannedFromPosting(permanentBannedMember);
        }

        @Test
        @DisplayName("경고 제재 사용자는 게시글을 정상적으로 생성할 수 있다")
        void warning_banned_user_can_create_board() {
            // given
            Member warningMember = createMember(BanType.WARNING, null);
            BoardInsertRequest request = createBoardInsertRequest();

            doNothing().when(banValidator).throwIfBannedFromPosting(warningMember);
            doNothing().when(profanityCheckService).validateProfanity(anyString());
            when(boardService.createAndSaveBoard(any(), any())).thenReturn(board);
            doNothing().when(boardGameStyleService).mapGameStylesToBoard(any(), anyList());

            // when
            boardFacadeService.createBoard(request, warningMember);

            // then
            verify(banValidator).throwIfBannedFromPosting(warningMember);
            // 경고는 실제 제재가 아니므로 정상 로직이 실행되어야 함
            verify(profanityCheckService).validateProfanity(anyString());
            verify(boardService).createAndSaveBoard(any(), any());
            verify(boardGameStyleService).mapGameStylesToBoard(any(), anyList());
        }
    }

    @Nested
    @DisplayName("게시글 수정 제재 검증")
    class UpdateBoardBanValidationTest {

        @Test
        @DisplayName("제재된 사용자가 게시글을 수정하려고 하면 BanValidator에서 예외가 발생한다")
        void banned_user_cannot_update_board() {
            // given
            BoardUpdateRequest request = createBoardUpdateRequest();

            doThrow(new MemberException(ErrorCode.MEMBER_BANNED_FROM_POSTING))
                    .when(banValidator).throwIfBannedFromPosting(bannedMember);

            // when & then
            assertThatThrownBy(() -> boardFacadeService.updateBoard(request, bannedMember, 1L))
                    .isInstanceOf(MemberException.class)
                    .hasFieldOrPropertyWithValue("code", ErrorCode.MEMBER_BANNED_FROM_POSTING.getCode());

            // 제재 검증에서 실패하면 이후 로직은 실행되지 않아야 함
            verify(banValidator).throwIfBannedFromPosting(bannedMember);
            verify(profanityCheckService, never()).validateProfanity(anyString());
            verify(boardService, never()).updateBoard(any(), anyLong(), anyLong());
            verify(boardGameStyleService, never()).updateBoardGameStyles(any(), anyList());
        }

        @Test
        @DisplayName("제재되지 않은 사용자는 게시글을 정상적으로 수정할 수 있다")
        void normal_user_can_update_board() {
            // given
            BoardUpdateRequest request = createBoardUpdateRequest();

            doNothing().when(banValidator).throwIfBannedFromPosting(normalMember);
            doNothing().when(profanityCheckService).validateProfanity(anyString());
            when(boardService.updateBoard(any(), anyLong(), anyLong())).thenReturn(board);
            doNothing().when(boardGameStyleService).updateBoardGameStyles(any(), anyList());

            // when
            boardFacadeService.updateBoard(request, normalMember, 1L);

            // then
            verify(banValidator).throwIfBannedFromPosting(normalMember);
            // 제재 검증 통과 후 정상 로직이 실행되어야 함
            verify(profanityCheckService).validateProfanity(anyString());
            verify(boardService).updateBoard(any(), anyLong(), anyLong());
            verify(boardGameStyleService).updateBoardGameStyles(any(), anyList());
        }

        @Test
        @DisplayName("제재 검증은 다른 모든 검증보다 먼저 실행된다")
        void ban_validation_executes_before_other_validations() {
            // given
            BoardUpdateRequest request = createBoardUpdateRequest();

            doThrow(new MemberException(ErrorCode.MEMBER_BANNED_FROM_POSTING))
                    .when(banValidator).throwIfBannedFromPosting(bannedMember);

            // when & then
            assertThatThrownBy(() -> boardFacadeService.updateBoard(request, bannedMember, 1L))
                    .isInstanceOf(MemberException.class);

            // 제재 검증이 먼저 실행되고 실패하면 다른 검증은 실행되지 않아야 함
            verify(banValidator).throwIfBannedFromPosting(bannedMember);
            verify(profanityCheckService, never()).validateProfanity(anyString());
            verify(boardService, never()).updateBoard(any(), anyLong(), anyLong());
        }
    }

    @Nested
    @DisplayName("게시글 끌올 제재 검증")
    class BumpBoardBanValidationTest {

        @Test
        @DisplayName("제재된 사용자가 게시글을 끌올하려고 하면 BanValidator에서 예외가 발생한다")
        void banned_user_cannot_bump_board() {
            // given
            doThrow(new MemberException(ErrorCode.MEMBER_BANNED_FROM_POSTING))
                    .when(banValidator).throwIfBannedFromPosting(bannedMember);

            // when & then
            assertThatThrownBy(() -> boardFacadeService.bumpBoard(1L, bannedMember))
                    .isInstanceOf(MemberException.class)
                    .hasFieldOrPropertyWithValue("code", ErrorCode.MEMBER_BANNED_FROM_POSTING.getCode());

            // 제재 검증에서 실패하면 이후 로직은 실행되지 않아야 함
            verify(banValidator).throwIfBannedFromPosting(bannedMember);
            verify(boardService, never()).bumpBoard(anyLong(), anyLong());
        }

        @Test
        @DisplayName("제재되지 않은 사용자는 게시글을 정상적으로 끌올할 수 있다")
        void normal_user_can_bump_board() {
            // given
            Board bumpedBoard = Board.builder()
                    .member(normalMember)
                    .gameMode(GameMode.SOLO)
                    .mainP(Position.TOP)
                    .subP(Position.JUNGLE)
                    .wantP(List.of(Position.ANY))
                    .mike(Mike.AVAILABLE)
                    .content("test content")
                    .build();
            ReflectionTestUtils.setField(bumpedBoard, "id", 1L);
            ReflectionTestUtils.setField(bumpedBoard, "bumpTime", LocalDateTime.now());

            doNothing().when(banValidator).throwIfBannedFromPosting(normalMember);
            when(boardService.bumpBoard(anyLong(), anyLong())).thenReturn(bumpedBoard);

            // when
            boardFacadeService.bumpBoard(1L, normalMember);

            // then
            verify(banValidator).throwIfBannedFromPosting(normalMember);
            // 제재 검증 통과 후 정상 로직이 실행되어야 함
            verify(boardService).bumpBoard(anyLong(), anyLong());
        }

        @Test
        @DisplayName("제재 검증은 게시글 끌올에서도 다른 모든 검증보다 먼저 실행된다")
        void ban_validation_executes_first_in_bump_board() {
            // given
            doThrow(new MemberException(ErrorCode.MEMBER_BANNED_FROM_POSTING))
                    .when(banValidator).throwIfBannedFromPosting(bannedMember);

            // when & then
            assertThatThrownBy(() -> boardFacadeService.bumpBoard(1L, bannedMember))
                    .isInstanceOf(MemberException.class);

            // 제재 검증이 먼저 실행되고 실패하면 다른 로직은 실행되지 않아야 함
            verify(banValidator).throwIfBannedFromPosting(bannedMember);
            verify(boardService, never()).bumpBoard(anyLong(), anyLong());
        }

        @Test
        @DisplayName("영구 제재된 사용자는 게시글을 끌올할 수 없다")
        void permanently_banned_user_cannot_bump_board() {
            // given
            Member permanentBannedMember = createMember(BanType.PERMANENT, null);
            doThrow(new MemberException(ErrorCode.MEMBER_BANNED_FROM_POSTING))
                    .when(banValidator).throwIfBannedFromPosting(permanentBannedMember);

            // when & then
            assertThatThrownBy(() -> boardFacadeService.bumpBoard(1L, permanentBannedMember))
                    .isInstanceOf(MemberException.class)
                    .hasFieldOrPropertyWithValue("code", ErrorCode.MEMBER_BANNED_FROM_POSTING.getCode());

            verify(banValidator).throwIfBannedFromPosting(permanentBannedMember);
        }
    }

    // 헬퍼 메서드
    private Member createMember(BanType banType, LocalDateTime banExpireAt) {
        Member member = Member.createForGeneral(
                "test@example.com",
                "password",
                LoginType.GENERAL,
                "testUser",
                "KR1",
                Tier.BRONZE,
                1,
                50.0,
                Tier.BRONZE,
                1,
                50.0,
                10,
                5,
                true
        );

        if (banType != BanType.NONE) {
            member.applyBan(banType, banExpireAt);
        }

        // Set a default ID for test members
        ReflectionTestUtils.setField(member, "id", 3L);

        return member;
    }

    // 헬퍼 메서드 - Request 생성
    private BoardInsertRequest createBoardInsertRequest() {
        BoardInsertRequest request = new BoardInsertRequest();
        ReflectionTestUtils.setField(request, "gameMode", GameMode.SOLO);
        ReflectionTestUtils.setField(request, "mainP", Position.TOP);
        ReflectionTestUtils.setField(request, "subP", Position.JUNGLE);
        ReflectionTestUtils.setField(request, "wantP", List.of(Position.ANY));
        ReflectionTestUtils.setField(request, "mike", Mike.AVAILABLE);
        ReflectionTestUtils.setField(request, "gameStyles", List.of(1L, 2L));
        ReflectionTestUtils.setField(request, "contents", "test content");
        return request;
    }

    private BoardUpdateRequest createBoardUpdateRequest() {
        BoardUpdateRequest request = new BoardUpdateRequest();
        ReflectionTestUtils.setField(request, "gameMode", GameMode.SOLO);
        ReflectionTestUtils.setField(request, "mainP", Position.TOP);
        ReflectionTestUtils.setField(request, "subP", Position.JUNGLE);
        ReflectionTestUtils.setField(request, "wantP", List.of(Position.ANY));
        ReflectionTestUtils.setField(request, "mike", Mike.AVAILABLE);
        ReflectionTestUtils.setField(request, "gameStyles", List.of(1L, 2L));
        ReflectionTestUtils.setField(request, "contents", "updated content");
        return request;
    }
}
