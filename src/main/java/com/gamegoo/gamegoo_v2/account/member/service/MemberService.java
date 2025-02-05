package com.gamegoo.gamegoo_v2.account.member.service;

import com.gamegoo.gamegoo_v2.account.auth.dto.request.JoinRequest;
import com.gamegoo.gamegoo_v2.account.member.domain.LoginType;
import com.gamegoo.gamegoo_v2.account.member.domain.Member;
import com.gamegoo.gamegoo_v2.account.member.domain.Mike;
import com.gamegoo.gamegoo_v2.account.member.domain.Position;
import com.gamegoo.gamegoo_v2.account.member.domain.Tier;
import com.gamegoo.gamegoo_v2.account.member.repository.MemberRepository;
import com.gamegoo.gamegoo_v2.core.exception.MemberException;
import com.gamegoo.gamegoo_v2.core.exception.common.ErrorCode;
import com.gamegoo.gamegoo_v2.external.riot.dto.TierDetails;
import com.gamegoo.gamegoo_v2.matching.domain.GameMode;
import com.gamegoo.gamegoo_v2.utils.PasswordUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;

    @Transactional
    public Member createMember(JoinRequest request, List<TierDetails> tiers) {

        // 기본 값 설정
        Tier soloTier = Tier.UNRANKED;
        int soloRank = 0;
        double soloWinRate = 0.0;
        int soloGameCount = 0;

        Tier freeTier = Tier.UNRANKED;
        int freeRank = 0;
        double freeWinRate = 0.0;
        int freeGameCount = 0;

        // 티어 정보 설정
        for (TierDetails tierDetail : tiers) {
            if (tierDetail.getGameMode() == GameMode.SOLO) {
                soloTier = tierDetail.getTier();
                soloRank = tierDetail.getRank();
                soloWinRate = tierDetail.getWinrate();
                soloGameCount = tierDetail.getGameCount();
            } else if (tierDetail.getGameMode() == GameMode.FREE) {
                freeTier = tierDetail.getTier();
                freeRank = tierDetail.getRank();
                freeWinRate = tierDetail.getWinrate();
                freeGameCount = tierDetail.getGameCount();
            }
        }

        // Member 생성
        Member member = Member.create(
                request.getEmail(),
                PasswordUtil.encodePassword(request.getPassword()),
                LoginType.GENERAL,
                request.getGameName(),
                request.getTag(),
                soloTier, soloRank, soloWinRate,
                freeTier, freeRank, freeWinRate,
                soloGameCount, freeGameCount, true
        );

        memberRepository.save(member);
        return member;
    }


    /**
     * 회원 정보 조회
     *
     * @param memberId 사용자 ID
     * @return Member
     */
    public Member findMemberById(Long memberId) {
        return memberRepository.findById(memberId).orElseThrow(() -> new MemberException(ErrorCode.MEMBER_NOT_FOUND));
    }

    /**
     * Email로 회원 정보 조회
     *
     * @param email 사용자 ID
     * @return Member
     */
    public Member findMemberByEmail(String email) {
        return memberRepository.findByEmail(email).orElseThrow(() -> new MemberException(ErrorCode.MEMBER_NOT_FOUND));
    }

    /**
     * Email 중복 확인하기
     *
     * @param email email
     */
    public void checkDuplicateMemberByEmail(String email) {
        if (memberRepository.existsByEmail(email)) {
            throw new MemberException(ErrorCode.MEMBER_ALREADY_EXISTS);
        }
    }

    /**
     * DB에 없는 사용자일 경우 예외 발생
     *
     * @param email email
     */
    public void checkExistMemberByEmail(String email) {
        if (!memberRepository.existsByEmail(email)) {
            throw new MemberException(ErrorCode.MEMBER_NOT_FOUND);
        }
    }

    /**
     * 프로필 이미지 수정
     *
     * @param member       회원
     * @param profileImage 프로필이미지
     */
    @Transactional
    public void setProfileImage(Member member, int profileImage) {
        member.updateProfileImage(profileImage);
    }

    /**
     * 마이크 여부 수정
     *
     * @param member 회원
     * @param mike   마이크 상태
     */
    @Transactional
    public void setIsMike(Member member, Mike mike) {
        member.updateMike(mike);
    }

    /**
     * 포지션 수정
     *
     * @param member       회원
     * @param mainPosition 주 포지션
     * @param subPosition  부 포지션
     * @param wantPosition 원하는 포지션
     */
    @Transactional
    public void setPosition(Member member, Position mainPosition, Position subPosition, Position wantPosition) {
        member.updatePosition(mainPosition, subPosition, wantPosition);
    }


    @Transactional
    public void updateMikePosition(Member member, Mike mike, Position mainP, Position subP, Position wantP) {
        // 마이크, 포지션 수정
        member.updateMemberByMatchingRecord(mike, mainP, subP, wantP);
    }

    /**
     * Member blind 처리
     * @param member 회원
     */
    @Transactional
    public void deactivateMember(Member member) {
        member.deactiveMember();
    }

}
