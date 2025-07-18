package com.gamegoo.gamegoo_v2.account.member.service;

import com.gamegoo.gamegoo_v2.account.auth.domain.Role;
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
import com.gamegoo.gamegoo_v2.external.riot.dto.request.RiotJoinRequest;
import com.gamegoo.gamegoo_v2.matching.domain.GameMode;
import com.gamegoo.gamegoo_v2.utils.PasswordUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;

    @PersistenceContext
    EntityManager em;

    @Transactional
    public Member createMemberGeneral(JoinRequest request, List<TierDetails> tiers) {

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
        Member member = Member.createForGeneral(
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

    @Transactional
    public Member createMemberRiot(RiotJoinRequest request, String gameName, String tag, List<TierDetails> tiers) {

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
        Member member = Member.createForRiot(
                request.getPuuid(), LoginType.RSO, gameName, tag,
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
     * Member 엔티티 리스트 조회
     *
     * @param memberIds
     * @return
     */
    public List<Member> findAllMemberByIds(List<Long> memberIds) {
        return memberRepository.findAllByIdIn(memberIds);
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
     * Puuid 중복 확인하기
     *
     * @param puuid puuid
     */
    public void checkDuplicateMemberByPuuid(String puuid) {
        if (memberRepository.existsByPuuid(puuid)) {
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
     * @param member        회원
     * @param mainPosition  주 포지션
     * @param subPosition   부 포지션
     * @param wantPositions 원하는 포지션 리스트
     */
    @Transactional
    public void setPosition(Member member, Position mainPosition, Position subPosition, List<Position> wantPositions) {
        member.updatePosition(mainPosition, subPosition, wantPositions);
    }


    /**
     * 마이크, 포지션 수정
     *
     * @param member        회원
     * @param mike          마이크 유무
     * @param mainP         주 포지션
     * @param subP          부 포지션
     * @param wantPositions 원하는 포지션 리스트
     */
    @Transactional
    public void updateMikePosition(Member member, Mike mike, Position mainP, Position subP,
                                   List<Position> wantPositions) {
        member.updateMemberByMatchingRecord(mike, mainP, subP, wantPositions);
    }


    @Transactional
    public List<Member> findMemberByPuuid(String puuid) {
        return memberRepository.findByPuuid(puuid);
    }

    /**
     * Member blind 처리
     *
     * @param member 회원
     */
    @Transactional
    public void deactivateMember(Member member) {
        member.deactiveMember();
        em.flush();
    }

    /**
     * 회원 역할(권한) 수정
     *
     * @param member 회원
     * @param role   변경할 역할
     */
    @Transactional
    public void updateMemberRole(Member member, Role role) {
        member.updateRole(role);
    }

}
