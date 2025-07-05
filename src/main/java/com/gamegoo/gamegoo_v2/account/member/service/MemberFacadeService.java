package com.gamegoo.gamegoo_v2.account.member.service;

import com.gamegoo.gamegoo_v2.account.auth.domain.Role;
import com.gamegoo.gamegoo_v2.account.member.domain.Member;
import com.gamegoo.gamegoo_v2.account.member.dto.request.GameStyleRequest;
import com.gamegoo.gamegoo_v2.account.member.dto.request.IsMikeRequest;
import com.gamegoo.gamegoo_v2.account.member.dto.request.PositionRequest;
import com.gamegoo.gamegoo_v2.account.member.dto.request.ProfileImageRequest;
import com.gamegoo.gamegoo_v2.account.member.dto.response.MyProfileResponse;
import com.gamegoo.gamegoo_v2.account.member.dto.response.OtherProfileResponse;
import com.gamegoo.gamegoo_v2.social.block.service.BlockService;
import com.gamegoo.gamegoo_v2.social.friend.service.FriendService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;



@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberFacadeService {

    private final MemberService memberService;
    private final FriendService friendService;
    private final BlockService blockService;
    private final MemberGameStyleService memberGameStyleService;
    private final ChampionStatsRefreshService championStatsRefreshService;

    /**
     * 내 프로필 조회
     *
     * @param member 조회할 회원
     * @return 조회된 결과 DTO
     */
    @Transactional
    public MyProfileResponse getMyProfile(Member member) {
        // 프로필 접근 시 최근 전적 챔피언 정보 자동 갱신
        refreshChampionStatsIfNeeded(member);
        
        // 업데이트된 데이터를 반영하기 위해 fresh entity 로딩
        Member freshMember = memberService.findMemberById(member.getId());
        return MyProfileResponse.of(freshMember);
    }

    /**
     * 다른 사람 프로필 조회
     *
     * @param member 조회할 회원
     * @return 조회된 결과 DTO
     */
    @Transactional
    public OtherProfileResponse getOtherProfile(Member member, Long targetMemberId) {

        // memberId로 targetMember 얻기
        Member targetMember = memberService.findMemberById(targetMemberId);

        // 프로필 접근 시 최근 전적 챔피언 정보 자동 갱신
        refreshChampionStatsIfNeeded(targetMember);

        // 업데이트된 데이터를 반영하기 위해 fresh entity 로딩
        targetMember = memberService.findMemberById(targetMemberId);

        // 친구 정보 얻기
        boolean isFriend = friendService.isFriend(member, targetMember);
        Long friendRequestMemberId = friendService.getFriendRequestMemberId(member, targetMember);

        // 차단된 사용자인지 확인
        boolean isBlocked = blockService.isBlocked(member, targetMember);

        return OtherProfileResponse.of(targetMember, isFriend, friendRequestMemberId,
                isBlocked);
    }

    /**
     * 프로필 접근 시 최근 전적 챔피언 정보 자동 갱신 (필요한 경우에만)
     *
     * @param member 갱신 대상 사용자
     */
    private void refreshChampionStatsIfNeeded(Member member) {
        // 마지막 챔피언 통계 갱신 시간 체크 (5분마다 갱신)
        LocalDateTime lastRefreshTime = member.getChampionStatsRefreshedAt();
        LocalDateTime now = LocalDateTime.now();

        // 5분 이상 지났거나, 처음 접근하는 경우 갱신
        if (lastRefreshTime == null ||
            ChronoUnit.MINUTES.between(lastRefreshTime, now) >= 5) {
            try {
                championStatsRefreshService.refreshChampionStats(member);
                // 갱신 성공 시에만 시간 업데이트
                member.updateChampionStatsRefreshedAt();
            } catch (Exception e) {
                // 갱신에 실패하더라도 프로필 조회는 정상적으로 진행되어야 하므로,
                // 트랜잭션을 분리하고 예외를 전파하지 않습니다.
            }
        }
    }

    /**
     * 프로필 이미지 수정
     *
     * @param member  회원
     * @param request 프로필이미지
     * @return 성공 메세지
     */
    @Transactional
    public String setProfileImage(Member member, ProfileImageRequest request) {
        memberService.setProfileImage(member, request.getProfileImage());
        return "프로필 이미지 수정이 완료됐습니다";
    }

    /**
     * 마이크 여부 수정
     *
     * @param member  회원
     * @param request 마이크 여부
     * @return 성공 메세지
     */
    @Transactional
    public String setMike(Member member, IsMikeRequest request) {
        memberService.setIsMike(member, request.getMike());
        return "마이크 여부 수정이 완료됐습니다";
    }

    /**
     * 포지션 수정
     *
     * @param member  회원
     * @param request 주/부/원하는 포지션
     * @return 성공 메세지
     */
    @Transactional
    public String setPosition(Member member, PositionRequest request) {
        memberService.setPosition(member, request.getMainP(), request.getSubP(), request.getWantP());
        return "포지션 여부 수정이 완료됐습니다";
    }

    /**
     * 게임 스타일 수정
     *
     * @param member  사용자
     * @param request 게임스타일 리스트
     * @return 성공 메세지
     */
    @Transactional
    public String setGameStyle(Member member, GameStyleRequest request) {
        memberGameStyleService.updateGameStyle(member, request.getGameStyleIdList());
        return "게임 스타일 수정이 완료되었습니다";
    }

    /**
     * 회원의 챔피언 통계 갱신(새로고침) 기능.
     * 회원이 새로고침 버튼을 누르면 호출됩니다.
     *
     * @param member 갱신 대상 사용자
     * @return 성공 메세지
     */
    @Transactional
    public String refreshChampionStats(Member member) {
        championStatsRefreshService.refreshChampionStats(member);
        return "챔피언 통계 갱신이 완료되었습니다";
    }

    /**
     * 회원의 역할(권한) 변경 기능.
     * 개발용으로 어드민 권한을 부여하거나 해제할 때 사용됩니다.
     *
     * @param memberId 대상 회원 ID
     * @param role 변경할 역할
     * @return 성공 메시지
     */
    @Transactional
    public String updateMemberRole(Long memberId, Role role) {
        Member member = memberService.findMemberById(memberId);
        memberService.updateMemberRole(member, role);
        return "회원 권한 변경이 완료되었습니다";
    }

}
