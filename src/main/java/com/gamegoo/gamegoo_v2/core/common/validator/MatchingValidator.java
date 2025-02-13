package com.gamegoo.gamegoo_v2.core.common.validator;

import com.gamegoo.gamegoo_v2.core.exception.common.ErrorCode;
import com.gamegoo.gamegoo_v2.core.exception.common.GlobalException;
import com.gamegoo.gamegoo_v2.matching.domain.MatchingRecord;
import com.gamegoo.gamegoo_v2.matching.domain.MatchingStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MatchingValidator extends BaseValidator {

    /**
     * 매칭 요청의 상태가 올바른지 검증합니다.
     *
     * @param status         요청하려는 매칭 상태
     * @param matchingRecord 현재 매칭 기록
     * @param exceptionClass 예외 클래스
     * @param errorCode      에러 코드
     */
    public <T extends GlobalException> void throwIfInvalidStatus(MatchingRecord matchingRecord, MatchingStatus status,
                                                                 Class<T> exceptionClass, ErrorCode errorCode) {
        if (status != matchingRecord.getStatus()) {
            throw createException(exceptionClass, errorCode);
        }
    }

}
