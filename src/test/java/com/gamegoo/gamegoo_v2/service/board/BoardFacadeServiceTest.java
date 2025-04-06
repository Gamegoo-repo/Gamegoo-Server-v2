package com.gamegoo.gamegoo_v2.service;

import com.gamegoo.gamegoo_v2.account.member.domain.LoginType;
import com.gamegoo.gamegoo_v2.account.member.domain.Member;
import com.gamegoo.gamegoo_v2.account.member.domain.Mike;
import com.gamegoo.gamegoo_v2.account.member.domain.Position;
import com.gamegoo.gamegoo_v2.account.member.domain.Tier;
import com.gamegoo.gamegoo_v2.account.member.service.MemberService;
import com.gamegoo.gamegoo_v2.content.board.domain.Board;
import com.gamegoo.gamegoo_v2.content.board.dto.response.BoardByIdResponseForMember;
import com.gamegoo.gamegoo_v2.content.board.repository.BoardRepository;
import com.gamegoo.gamegoo_v2.content.board.service.BoardFacadeService;
import com.gamegoo.gamegoo_v2.content.board.service.BoardGameStyleService;
import com.gamegoo.gamegoo_v2.content.board.service.BoardService;
import com.gamegoo.gamegoo_v2.content.board.service.ProfanityCheckService;
import com.gamegoo.gamegoo_v2.matching.domain.GameMode;
import com.gamegoo.gamegoo_v2.social.block.service.BlockService;
import com.gamegoo.gamegoo_v2.social.friend.service.FriendService;
import com.gamegoo.gamegoo_v2.social.manner.service.MannerService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BoardFacadeServiceTest {

    @Mock
    private BoardService boardService;

    @Mock
    private BoardGameStyleService boardGameStyleService;

    @Mock
    private MemberService memberService;

    @Mock
    private FriendService friendService;

    @Mock
    private BlockService blockService;

    @Mock
    private ProfanityCheckService profanityCheckService;

    @Mock
    private MannerService mannerService;

    @InjectMocks
    private BoardFacadeService boardFacadeService;

    @Test
    @DisplayName("게시글 상세 조회 시 매너 정보가 정상적으로 반환되는지 테스트")
    void getBoardByIdForMember_ShouldReturnMannerInfo() {
        // given
        Member poster = Member.create(
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

        Member viewer = Member.create(
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
                "test content",
                1
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
} 