package com.gamegoo.gamegoo_v2.matching.service;

import com.gamegoo.gamegoo_v2.account.member.domain.Member;
import com.gamegoo.gamegoo_v2.account.member.service.MemberGameStyleService;
import com.gamegoo.gamegoo_v2.account.member.service.MemberService;
import com.gamegoo.gamegoo_v2.chat.domain.Chatroom;
import com.gamegoo.gamegoo_v2.chat.service.ChatCommandService;
import com.gamegoo.gamegoo_v2.chat.service.ChatQueryService;
import com.gamegoo.gamegoo_v2.core.common.validator.BlockValidator;
import com.gamegoo.gamegoo_v2.core.common.validator.MemberValidator;
import com.gamegoo.gamegoo_v2.core.exception.ChatException;
import com.gamegoo.gamegoo_v2.core.exception.common.ErrorCode;
import com.gamegoo.gamegoo_v2.matching.domain.MatchingRecord;
import com.gamegoo.gamegoo_v2.matching.dto.request.InitializingMatchingRequest;
import com.gamegoo.gamegoo_v2.matching.dto.request.ModifyMyMatchingStatusRequest;
import com.gamegoo.gamegoo_v2.matching.dto.response.PriorityListResponse;
import com.gamegoo.gamegoo_v2.social.block.service.BlockService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MatchingFacadeService {

    private final ChatQueryService chatQueryService;
    private final ChatCommandService chatCommandService;
    private final MemberValidator memberValidator;
    private final BlockValidator blockValidator;
    private final MemberService memberService;
    private final MatchingService matchingService;
    private final BlockService blockService;
    private final MemberGameStyleService memberGameStyleService;

    /**
     * 매칭 우선순위 계산 및 DB 저장
     *
     * @param memberId 회원 ID
     * @param request  회원 정보
     * @return 매칭 정보
     */
    @Transactional
    public PriorityListResponse calculatePriorityAndRecording(Long memberId, InitializingMatchingRequest request) {
        // 사용자 조회
        Member member = memberService.findMemberById(memberId);

        // 매칭 정보로 member 업데이트
        // 마이크, 포지션 변경
        memberService.updateMikePosition(member, request.getMike(), request.getMainP(), request.getSubP(),
                request.getWantP());
        // 게임스타일 변경
        memberGameStyleService.updateGameStyle(member, request.getGameStyleIdList());

        // matchingRecord DB에 저장
        MatchingRecord matchingRecord = matchingService.createMatchingRecord(member, request.getMatchingType(),
                request.getGameMode());

        // 현재 대기 중인 사용자 조회
        List<MatchingRecord> pendingMatchingRecords = matchingService.getPendingMatchingRecords(request.getGameMode());

        // 차단당한 사용자 제외
        List<Long> targetMemberIds = new ArrayList<>();
        for (MatchingRecord record : pendingMatchingRecords) {
            targetMemberIds.add(record.getMember().getId());
        }

        // 차단 여부 확인
        Map<Long, Boolean> blockedStatusMap = blockService.isBlockedByTargetMembersBatch(member, targetMemberIds);

        // 차단되지 않은 사용자만 필터링
        List<MatchingRecord> filteredPendingMatchingRecords = new ArrayList<>();
        for (MatchingRecord record : pendingMatchingRecords) {
            Long targetMemberId = record.getMember().getId();
            if (!blockedStatusMap.getOrDefault(targetMemberId, false)) {
                filteredPendingMatchingRecords.add(record);
            }
        }

        // myPriorityList, otherPriorityList 조회
        return matchingService.calculatePriorityList(matchingRecord, filteredPendingMatchingRecords);
    }

    /**
     * 나의 matching Status 변경
     *
     * @param request request
     * @return 성공 메시지
     */
    @Transactional
    public String modifyMyMatchingStatus(ModifyMyMatchingStatusRequest request) {
        // matching 조회
        MatchingRecord matchingRecordByMatchingUuid =
                matchingService.getMatchingRecordByMatchingUuid(request.getMatchingUuid());

        // 변경
        matchingService.setMatchingStatus(request.getStatus(), matchingRecordByMatchingUuid);

        return "status 변경이 완료되었습니다.";
    }

    /**
     * 두 회원 사이 매칭을 통한 채팅방 시작 Facade 메소드
     *
     * @param member1 회원
     * @param member2 회원
     * @return 채팅방 uuid
     */
    @Transactional
    public String startChatroomByMatching(Member member1, Member member2) {
        memberValidator.throwIfEqual(member1, member2);

        // 탈퇴하지 않았는지 검증
        memberValidator.throwIfBlind(member1);
        memberValidator.throwIfBlind(member2);

        // 서로의 차단 여부 검증
        validateBlockStatus(member1, member2);

        // 채팅방 조회, 생성 및 입장 처리
        Chatroom chatroom = chatQueryService.findExistingChatroom(member1, member2)
                .orElseGet(() -> chatCommandService.createChatroom(member1, member2));

        chatCommandService.updateLastJoinDate(member1, chatroom.getId(), LocalDateTime.now());
        chatCommandService.updateLastJoinDate(member2, chatroom.getId(), LocalDateTime.now());

        // 매칭 시스템 메시지 생성
        chatCommandService.createMatchingSystemChat(member1, member2, chatroom);

        return chatroom.getUuid();
    }

    /**
     * 두 회원의 서로 차단 여부 검증
     *
     * @param member1 회원
     * @param member2 회원
     */
    private void validateBlockStatus(Member member1, Member member2) {
        blockValidator.throwIfBlocked(member1, member2, ChatException.class,
                ErrorCode.CHAT_START_FAILED_TARGET_IS_BLOCKED);
        blockValidator.throwIfBlocked(member2, member1, ChatException.class,
                ErrorCode.CHAT_START_FAILED_BLOCKED_BY_TARGET);
    }

}
