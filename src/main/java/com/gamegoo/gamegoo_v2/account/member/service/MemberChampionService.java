package com.gamegoo.gamegoo_v2.account.member.service;

import com.gamegoo.gamegoo_v2.account.member.domain.Member;
import com.gamegoo.gamegoo_v2.account.member.domain.MemberChampion;
import com.gamegoo.gamegoo_v2.account.member.repository.MemberChampionRepository;
import com.gamegoo.gamegoo_v2.external.riot.domain.ChampionStats;
import com.gamegoo.gamegoo_v2.game.repository.ChampionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberChampionService {

    private enum ChampionStatsMode {
        SOLO, FREE, ARAM
    }

    private final MemberChampionRepository memberChampionRepository;
    private final ChampionRepository championRepository;

    /**
     * 멤버와 챔피언 ID 목록을 기반으로 MemberChampion 엔티티를 생성 및 저장하는 메서드
     * 기존 데이터를 삭제하고 새로운 데이터를 저장합니다.
     *
     * @param member        대상 멤버
     * @param championStats 챔피언 ID 목록
     */
    @Transactional
    public void saveMemberChampions(Member member, List<ChampionStats> championStats) {
        // 새로운 챔피언 데이터 저장 (기존 데이터는 호출하는 쪽에서 삭제)
        championStats.forEach(stats -> {
            Long championId = stats.getChampionId();
            // 챔피언이 존재하지 않으면 스킵
            championRepository.findById(championId).ifPresent(champion -> {
                MemberChampion memberChampion = MemberChampion.create(champion, member, stats.getWins(),
                        stats.getGames(), stats.getCsPerMinute(), stats.getTotalCs(), stats.getKills(), stats.getDeaths(), stats.getAssists());
                memberChampionRepository.save(memberChampion);
            });
        });
    }

    @Transactional
    public void saveMemberChampionsByMode(Member member, List<ChampionStats> soloStats, 
                                         List<ChampionStats> freeStats, List<ChampionStats> aramStats) {
        // 솔로랭크 통계 업데이트
        updateChampionStatsByMode(member, soloStats, ChampionStatsMode.SOLO);
        
        // 자유랭크 통계 업데이트
        updateChampionStatsByMode(member, freeStats, ChampionStatsMode.FREE);
        
        // 칼바람 통계 업데이트
        updateChampionStatsByMode(member, aramStats, ChampionStatsMode.ARAM);
    }

    /**
     * 기존 MemberChampion 레코드에만 모드별 통계를 업데이트 (새로운 챔피언 생성하지 않음)
     */
    @Transactional
    public void updateMemberChampionsByMode(Member member, List<ChampionStats> soloStats, 
                                           List<ChampionStats> freeStats, List<ChampionStats> aramStats) {
        // 솔로랭크 통계 업데이트 (기존 레코드만)
        updateExistingChampionStatsByMode(member, soloStats, ChampionStatsMode.SOLO);
        
        // 자유랭크 통계 업데이트 (기존 레코드만)
        updateExistingChampionStatsByMode(member, freeStats, ChampionStatsMode.FREE);
        
        // 칼바람 통계 업데이트 (기존 레코드만)
        updateExistingChampionStatsByMode(member, aramStats, ChampionStatsMode.ARAM);
    }

    /**
     * 모드별 챔피언 통계 업데이트 공통 메서드
     */
    private void updateChampionStatsByMode(Member member, List<ChampionStats> championStatsList, ChampionStatsMode mode) {
        championStatsList.forEach(stats -> {
            Long championId = stats.getChampionId();
            championRepository.findById(championId).ifPresent(champion -> {
                MemberChampion memberChampion = memberChampionRepository.findByMemberAndChampion(member, champion)
                        .orElseGet(() -> {
                            MemberChampion newMc = MemberChampion.create(champion, member, 0, 0, 0.0, 0, 0, 0, 0);
                            return memberChampionRepository.save(newMc);
                        });
                
                updateStatsByMode(memberChampion, stats, mode);
            });
        });
    }

    /**
     * 기존 레코드에만 모드별 챔피언 통계 업데이트 (새로운 챔피언 생성하지 않음)
     */
    private void updateExistingChampionStatsByMode(Member member, List<ChampionStats> championStatsList, ChampionStatsMode mode) {
        championStatsList.forEach(stats -> {
            Long championId = stats.getChampionId();
            championRepository.findById(championId).ifPresent(champion -> {
                memberChampionRepository.findByMemberAndChampion(member, champion)
                        .ifPresent(memberChampion -> updateStatsByMode(memberChampion, stats, mode));
            });
        });
    }

    /**
     * 모드에 따라 적절한 업데이트 메서드 호출
     */
    private void updateStatsByMode(MemberChampion memberChampion, ChampionStats stats, ChampionStatsMode mode) {
        switch (mode) {
            case SOLO -> memberChampion.updateSoloStats(stats.getWins(), stats.getGames(), stats.getCsPerMinute(), 
                    stats.getTotalCs(), stats.getKills(), stats.getDeaths(), stats.getAssists());
            case FREE -> memberChampion.updateFreeStats(stats.getWins(), stats.getGames(), stats.getCsPerMinute(), 
                    stats.getTotalCs(), stats.getKills(), stats.getDeaths(), stats.getAssists());
            case ARAM -> memberChampion.updateAramStats(stats.getWins(), stats.getGames(), stats.getCsPerMinute(), 
                    stats.getTotalCs(), stats.getKills(), stats.getDeaths(), stats.getAssists());
        }
    }

}
