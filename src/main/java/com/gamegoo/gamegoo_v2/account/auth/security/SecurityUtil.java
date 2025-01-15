package com.gamegoo.gamegoo_v2.account.auth.security;

import com.gamegoo.gamegoo_v2.core.exception.AuthException;
import com.gamegoo.gamegoo_v2.core.exception.common.ErrorCode;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtil {

    public static Long getCurrentMemberId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails customUserDetails) {
            return customUserDetails.getMemberId();
        } else {
            throw new AuthException(ErrorCode.UNAUTHORIZED_EXCEPTION);
        }

    }
}
