package com.gamegoo.gamegoo_v2.core.exception;

import com.gamegoo.gamegoo_v2.core.exception.common.ErrorCode;
import lombok.Getter;

@Getter
public class ChampionRefreshCooldownException extends RuntimeException {
    
    private final ErrorCode errorCode;
    private final long remainingHours;
    
    public ChampionRefreshCooldownException(ErrorCode errorCode, long remainingHours) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.remainingHours = remainingHours;
    }
    
    public String getDetailedMessage() {
        return String.format("%s %d시간 후 다시 시도해주세요.", errorCode.getMessage(), remainingHours);
    }
}