package com.gamegoo.gamegoo_v2.core.common.validator;

import com.gamegoo.gamegoo_v2.core.exception.common.ErrorCode;
import com.gamegoo.gamegoo_v2.core.exception.common.GlobalException;

public abstract class BaseValidator {

    /**
     * 해당 ErrorCode로 예외 생성
     *
     * @param exceptionClass 예외 클래스
     * @param errorCode      에러 코드
     * @return Exception
     */
    protected <T extends GlobalException> T createException(Class<T> exceptionClass, ErrorCode errorCode) {
        try {
            return exceptionClass.getConstructor(ErrorCode.class).newInstance(errorCode);
        } catch (Exception e) {
            throw new RuntimeException("Exception instantiation failed", e);
        }
    }

}
