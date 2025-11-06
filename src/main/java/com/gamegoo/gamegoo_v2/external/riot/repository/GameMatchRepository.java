package com.gamegoo.gamegoo_v2.external.riot.repository;

import com.gamegoo.gamegoo_v2.account.member.domain.Member;
import com.gamegoo.gamegoo_v2.external.riot.domain.GameMatch;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * GameMatch 엔티티에 대한 Repository
 */
public interface GameMatchRepository extends JpaRepository<GameMatch, Long> {

    /**
     * 특정 사용자의 특정 매치가 이미 DB에 저장되어 있는지 확인
     * - 증분 업데이트 시 중복 저장 방지용
     *
     * @param member  사용자
     * @param matchId Riot 매치 ID
     * @return 존재 여부
     */
    boolean existsByMemberAndMatchId(Member member, String matchId);

    /**
     * 특정 사용자의 최근 매치 N개를 gameStartedAt 기준 내림차순으로 조회
     * - 통계 계산용 (최근 30개 게임)
     *
     * @param member 사용자
     * @return 최근 매치 리스트 (최대 30개)
     */
    List<GameMatch> findTop30ByMemberOrderByGameStartedAtDesc(Member member);
}