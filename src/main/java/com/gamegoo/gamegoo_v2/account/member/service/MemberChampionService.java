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

    private final MemberChampionRepository memberChampionRepository;
    private final ChampionRepository championRepository;

    /**
     * 멤버와 챔피언 ID 목록을 기반으로 MemberChampion 엔티티를 생성 및 저장하는 메서드
     *
     * @param member        대상 멤버
     * @param championStats 챔피언 ID 목록
     */
    @Transactional
    public void saveMemberChampions(Member member, List<ChampionStats> championStats) {
        championStats.forEach(stats -> {
            Long championId = stats.getChampionId();
            // 챔피언이 존재하지 않으면 스킵
            championRepository.findById(championId).ifPresent(champion -> {
                MemberChampion memberChampion = MemberChampion.create(champion, member, stats.getWins(),
                        stats.getGames(), stats.getCsPerMinute(), stats.getKills(), stats.getDeaths(), stats.getAssists());
                memberChampionRepository.save(memberChampion);
            });
        });
    }


}
