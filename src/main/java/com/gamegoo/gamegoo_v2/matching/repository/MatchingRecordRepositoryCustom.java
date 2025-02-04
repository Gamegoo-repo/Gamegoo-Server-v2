package com.gamegoo.gamegoo_v2.matching.repository;

import com.gamegoo.gamegoo_v2.account.member.domain.Member;
import com.gamegoo.gamegoo_v2.matching.domain.GameMode;
import com.gamegoo.gamegoo_v2.matching.domain.MatchingRecord;

import java.time.LocalDateTime;
import java.util.List;

public interface MatchingRecordRepositoryCustom {

    /**
     * 매칭 가능한 유효한 레코드 조회 (5분 이내, 특정 게임 모드, PENDING 상태)
     *
     * @param createdAt 5분 전 시간 기준
     * @param gameMode  게임 모드
     * @return 매칭 가능한 레코드 리스트
     */
    List<MatchingRecord> findValidMatchingRecords(LocalDateTime createdAt, GameMode gameMode);

    /**
     * 가장 최근 기록 불러오기
     *
     * @param member 사용자
     * @return 사용자의 가장 최근 매칭 기록
     */
    MatchingRecord findLatestByMember(Member member);

}
