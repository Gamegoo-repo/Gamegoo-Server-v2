package com.gamegoo.gamegoo_v2.external.riot.service;

import com.gamegoo.gamegoo_v2.external.riot.domain.GameMatch;
import com.gamegoo.gamegoo_v2.external.riot.repository.GameMatchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * GameMatch 저장 전용 컴포넌트
 * 동시 전적 갱신 요청 시 개별 저장 실패가 전체 프로세스를 중단시키지 않도록 별도 트랜잭션으로 처리
 */
@Component
@RequiredArgsConstructor
public class GameMatchSaver {

    private final GameMatchRepository gameMatchRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveGameMatch(GameMatch gameMatch) {
        gameMatchRepository.save(gameMatch);
    }
}
