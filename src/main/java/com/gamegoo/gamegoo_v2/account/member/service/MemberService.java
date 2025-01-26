package com.gamegoo.gamegoo_v2.account.member.service;

import com.gamegoo.gamegoo_v2.account.member.domain.LoginType;
import com.gamegoo.gamegoo_v2.account.member.domain.Member;
import com.gamegoo.gamegoo_v2.account.member.domain.Mike;
import com.gamegoo.gamegoo_v2.account.member.domain.Position;
import com.gamegoo.gamegoo_v2.account.member.domain.Tier;
import com.gamegoo.gamegoo_v2.account.member.repository.MemberRepository;
import com.gamegoo.gamegoo_v2.core.exception.MemberException;
import com.gamegoo.gamegoo_v2.core.exception.common.ErrorCode;
import com.gamegoo.gamegoo_v2.utils.PasswordUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;
    private final MemberGameStyleService memberGameStyleService;

    /**
     * Member 생성 메소드
     *
     * @param email     이메일
     * @param password  비밀번호
     * @param gameName  소환사명
     * @param tag       태그
     * @param tier      티어
     * @param rank      랭크
     * @param winrate   승률
     * @param gameCount 총 게임 횟수
     * @param isAgree   개인정보 동의
     * @return Member
     */
    @Transactional
    public Member createMember(String email, String password, String gameName, String tag, Tier tier, int rank,
                               double winrate, int gameCount, boolean isAgree) {
        Member member = Member.create(email, PasswordUtil.encodePassword(password), LoginType.GENERAL, gameName, tag,
                tier, rank, winrate, gameCount, isAgree);
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

}
