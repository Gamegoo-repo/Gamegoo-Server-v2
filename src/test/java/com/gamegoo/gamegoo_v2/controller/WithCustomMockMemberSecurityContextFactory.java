package com.gamegoo.gamegoo_v2.controller;

import com.gamegoo.gamegoo_v2.account.auth.domain.Role;
import com.gamegoo.gamegoo_v2.account.auth.security.CustomUserDetails;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

public class WithCustomMockMemberSecurityContextFactory implements WithSecurityContextFactory<WithCustomMockMember> {

    @Override
    public SecurityContext createSecurityContext(WithCustomMockMember annotation) {
        Long memberId = annotation.memberId;

        UserDetails userDetails = new CustomUserDetails(memberId, Role.MEMBER);

        // UserDetails, Password, Role -> 접근권한 인증 Token 생성
        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

        //현재 Request의 Security Context에 접근권한 설정
        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(usernamePasswordAuthenticationToken);
        
        return context;
    }

}
