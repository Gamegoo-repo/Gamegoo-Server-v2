package com.gamegoo.gamegoo_v2.matching.repository;

import com.gamegoo.gamegoo_v2.account.member.domain.Member;
import com.gamegoo.gamegoo_v2.matching.domain.GameMode;
import com.gamegoo.gamegoo_v2.matching.domain.MatchingRecord;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface MatchingRecordRepositoryCustom {

    /**
     * 매칭 가능한 유효한 레코드 조회
     *
     * @param createdAt 생성 시간
     * @param gameMode  게임 모드
     * @param memberId  사용자 id
     * @return 매칭 가능한 레코드 리스트
     */
    List<MatchingRecord> findValidMatchingRecords(LocalDateTime createdAt, GameMode gameMode, Long memberId);

    /**
     * 가장 최근 기록 불러오기 - member
     *
     * @param member 사용자
     * @return 사용자의 가장 최근 매칭 기록
     */
    Optional<MatchingRecord> findLatestByMember(Member member);

    /**
     * 해당 matchingRecord의 매칭 상대 회원 엔티티 조회
     *
     * @param uuid matchingRecord UUID
     * @return 매칭 상대 회원, 매칭 상대가 존재하지 않으면 Optional empty를 리턴
     */
    Optional<Member> findTargetMemberByUuid(String uuid);

}
