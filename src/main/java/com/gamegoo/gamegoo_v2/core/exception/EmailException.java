package com.gamegoo.gamegoo_v2.core.exception;

import com.gamegoo.gamegoo_v2.core.exception.common.ErrorCode;
import com.gamegoo.gamegoo_v2.core.exception.common.GlobalException;

public class EmailException extends GlobalException {

    public EmailException(ErrorCode errorCode) {
        super(errorCode);
    }

}
