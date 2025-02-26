package com.gamegoo.gamegoo_v2.core.exception;

import com.gamegoo.gamegoo_v2.core.exception.common.ErrorCode;
import com.gamegoo.gamegoo_v2.core.exception.common.GlobalException;

public class JwtAuthException extends GlobalException {

    public JwtAuthException(ErrorCode errorCode) {
        super(errorCode);
    }

}
