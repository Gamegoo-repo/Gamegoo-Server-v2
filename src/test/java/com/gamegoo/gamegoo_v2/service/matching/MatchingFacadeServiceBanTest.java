package com.gamegoo.gamegoo_v2.service.matching;

import com.gamegoo.gamegoo_v2.account.member.domain.BanType;
import com.gamegoo.gamegoo_v2.account.member.domain.LoginType;
import com.gamegoo.gamegoo_v2.account.member.domain.Member;
import com.gamegoo.gamegoo_v2.account.member.domain.Mike;
import com.gamegoo.gamegoo_v2.account.member.domain.Position;
import com.gamegoo.gamegoo_v2.account.member.domain.Tier;
import com.gamegoo.gamegoo_v2.account.member.service.MemberGameStyleService;
import com.gamegoo.gamegoo_v2.account.member.service.MemberService;
import com.gamegoo.gamegoo_v2.chat.service.ChatCommandService;
import com.gamegoo.gamegoo_v2.chat.service.ChatQueryService;
import com.gamegoo.gamegoo_v2.core.common.validator.BanValidator;
import com.gamegoo.gamegoo_v2.core.common.validator.BlockValidator;
import com.gamegoo.gamegoo_v2.core.common.validator.MatchingValidator;
import com.gamegoo.gamegoo_v2.core.common.validator.MemberValidator;
import com.gamegoo.gamegoo_v2.core.exception.MemberException;
import com.gamegoo.gamegoo_v2.core.exception.common.ErrorCode;
import com.gamegoo.gamegoo_v2.matching.domain.GameMode;
import com.gamegoo.gamegoo_v2.matching.domain.MatchingType;
import com.gamegoo.gamegoo_v2.matching.dto.request.InitializingMatchingRequest;
import com.gamegoo.gamegoo_v2.matching.service.MatchingFacadeService;
import com.gamegoo.gamegoo_v2.matching.service.MatchingService;
import com.gamegoo.gamegoo_v2.social.block.service.BlockService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("MatchingFacadeService 제재 검증 테스트")
class MatchingFacadeServiceBanTest {

    @Mock
    private ChatQueryService chatQueryService;

    @Mock
    private ChatCommandService chatCommandService;

    @Mock
    private MemberValidator memberValidator;

    @Mock
    private BlockValidator blockValidator;

    @Mock
    private BanValidator banValidator;

    @Mock
    private MemberService memberService;

    @Mock
    private MatchingService matchingService;

    @Mock
    private BlockService blockService;

    @Mock
    private MemberGameStyleService memberGameStyleService;

    @Mock
    private MatchingValidator matchingValidator;

    @InjectMocks
    private MatchingFacadeService matchingFacadeService;

    private Member bannedMember;
    private Member normalMember;
    private InitializingMatchingRequest matchingRequest;

    @BeforeEach
    void setUp() {
        bannedMember = createMember(BanType.BAN_1D, LocalDateTime.now().plusDays(1));
        normalMember = createMember(BanType.NONE, null);

        matchingRequest = InitializingMatchingRequest.builder()
                .mike(Mike.AVAILABLE)
                .mainP(Position.TOP)
                .subP(Position.JUNGLE)
                .wantP(List.of(Position.ANY))
                .matchingType(MatchingType.BASIC)
                .gameMode(GameMode.SOLO)
                .gameStyleIdList(List.of(1L, 2L))
                .build();
    }

    @Test
    @DisplayName("제재된 사용자가 매칭을 시작하려고 하면 BanValidator에서 예외가 발생한다")
    void banned_user_cannot_start_matching() {
        // given
        when(memberService.findMemberById(anyLong())).thenReturn(bannedMember);
        doThrow(new MemberException(ErrorCode.MEMBER_BANNED_FROM_MATCHING))
                .when(banValidator).throwIfBannedFromMatching(bannedMember);

        // when & then
        assertThatThrownBy(() -> matchingFacadeService.calculatePriorityAndRecording(1L, matchingRequest))
                .isInstanceOf(MemberException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.MEMBER_BANNED_FROM_MATCHING.getCode());

        // 제재 검증에서 실패하면 이후 로직은 실행되지 않아야 함
        verify(banValidator).throwIfBannedFromMatching(bannedMember);
        verify(memberService, never()).updateMikePosition(any(), any(), any(), any(), any());
        verify(memberGameStyleService, never()).updateGameStyle(any(), any());
        verify(matchingService, never()).createMatchingRecord(any(), any(), any());
    }

    @Test
    @DisplayName("제재되지 않은 사용자는 매칭을 정상적으로 시작할 수 있다")
    void normal_user_can_start_matching() throws Exception {
        // given
        when(memberService.findMemberById(anyLong())).thenReturn(normalMember);
        doNothing().when(banValidator).throwIfBannedFromMatching(normalMember);

        // Mock 추가 설정
        doNothing().when(memberService).updateMikePosition(any(), any(), any(), any(), any());
        doNothing().when(memberGameStyleService).updateGameStyle(any(), any());
        when(matchingService.createMatchingRecord(any(), any(), any())).thenReturn(null);
        when(matchingService.getPendingMatchingRecords(any(), anyLong())).thenReturn(List.of());
        when(matchingService.calculatePriorityList(any(), any())).thenReturn(null);

        // when
        matchingFacadeService.calculatePriorityAndRecording(1L, matchingRequest);

        // then
        verify(banValidator).throwIfBannedFromMatching(normalMember);
        // 제재 검증 통과 후 정상 로직이 실행되어야 함
        verify(memberService).updateMikePosition(any(), any(), any(), any(), any());
        verify(memberGameStyleService).updateGameStyle(any(), any());
        verify(matchingService).createMatchingRecord(any(), any(), any());
    }

    @Test
    @DisplayName("영구 제재된 사용자는 매칭을 시작할 수 없다")
    void permanently_banned_user_cannot_start_matching() {
        // given
        Member permanentBannedMember = createMember(BanType.PERMANENT, null);
        when(memberService.findMemberById(anyLong())).thenReturn(permanentBannedMember);
        doThrow(new MemberException(ErrorCode.MEMBER_BANNED_FROM_MATCHING))
                .when(banValidator).throwIfBannedFromMatching(permanentBannedMember);

        // when & then
        assertThatThrownBy(() -> matchingFacadeService.calculatePriorityAndRecording(1L, matchingRequest))
                .isInstanceOf(MemberException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.MEMBER_BANNED_FROM_MATCHING.getCode());

        verify(banValidator).throwIfBannedFromMatching(permanentBannedMember);
    }

    @Test
    @DisplayName("경고 제재 사용자는 매칭을 정상적으로 시작할 수 있다")
    void warning_banned_user_can_start_matching() throws Exception {
        // given
        Member warningMember = createMember(BanType.WARNING, null);
        when(memberService.findMemberById(anyLong())).thenReturn(warningMember);
        doNothing().when(banValidator).throwIfBannedFromMatching(warningMember);

        // Mock 추가 설정
        doNothing().when(memberService).updateMikePosition(any(), any(), any(), any(), any());
        doNothing().when(memberGameStyleService).updateGameStyle(any(), any());
        when(matchingService.createMatchingRecord(any(), any(), any())).thenReturn(null);
        when(matchingService.getPendingMatchingRecords(any(), anyLong())).thenReturn(List.of());
        when(matchingService.calculatePriorityList(any(), any())).thenReturn(null);

        // when
        matchingFacadeService.calculatePriorityAndRecording(1L, matchingRequest);

        // then
        verify(banValidator).throwIfBannedFromMatching(warningMember);
        // 경고는 실제 제재가 아니므로 정상 로직이 실행되어야 함
        verify(memberService).updateMikePosition(any(), any(), any(), any(), any());
        verify(memberGameStyleService).updateGameStyle(any(), any());
        verify(matchingService).createMatchingRecord(any(), any(), any());
    }

    @Test
    @DisplayName("제재 검증은 다른 모든 검증보다 먼저 실행된다")
    void ban_validation_executes_before_other_validations() {
        // given
        when(memberService.findMemberById(anyLong())).thenReturn(bannedMember);
        doThrow(new MemberException(ErrorCode.MEMBER_BANNED_FROM_MATCHING))
                .when(banValidator).throwIfBannedFromMatching(bannedMember);

        // when & then
        assertThatThrownBy(() -> matchingFacadeService.calculatePriorityAndRecording(1L, matchingRequest))
                .isInstanceOf(MemberException.class);

        // 제재 검증이 먼저 실행되고 실패하면 다른 로직은 실행되지 않아야 함
        verify(banValidator).throwIfBannedFromMatching(bannedMember);
        verify(memberService, never()).updateMikePosition(any(), any(), any(), any(), any());
        verify(memberGameStyleService, never()).updateGameStyle(any(), any());
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

        return member;
    }
}
