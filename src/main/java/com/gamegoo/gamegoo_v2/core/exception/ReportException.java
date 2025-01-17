package com.gamegoo.gamegoo_v2.core.exception;

import com.gamegoo.gamegoo_v2.core.exception.common.ErrorCode;
import com.gamegoo.gamegoo_v2.core.exception.common.GlobalException;

public class ReportException extends GlobalException {

    public ReportException(ErrorCode errorCode) {
        super(errorCode);
    }

}
