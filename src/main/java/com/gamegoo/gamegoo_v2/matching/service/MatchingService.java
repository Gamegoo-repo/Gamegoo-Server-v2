package com.gamegoo.gamegoo_v2.matching.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MatchingService {

    private final MatchingPriorityCalculateService matchingPriorityCalculateService;

    //// TODO : 게임 모드, 매칭에 따라 우선순위 계산하는 서비스
    //public int calculatePriority() {
    //
    //}

    // TODO :  현재 PENDING 상태인 모든 매칭 대기자 조회

    // TODO : Matching Record 데이터 추가

}
