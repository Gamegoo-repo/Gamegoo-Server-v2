package com.gamegoo.gamegoo_v2.account.auth.security;

import com.gamegoo.gamegoo_v2.account.auth.domain.Role;
import com.gamegoo.gamegoo_v2.account.auth.jwt.JwtProvider;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final List<RequestMatcher> excludedRequestMatchers;
    private final JwtProvider jwtProvider;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        // JWT Filter를 사용하지 않는 Path는 제외
        if (excludedRequestMatchers.stream().anyMatch(m -> m.matches(request))) {
            filterChain.doFilter(request, response);
            return;
        }

        String jwt = jwtProvider.resolveToken(request); // request header에서 jwt토큰 값만을 추출, 토큰 값이 빈 상태로 요청 온 경우 null

        if (StringUtils.hasText(jwt) && jwtProvider.validateToken(jwt)) {
            Long memberId = jwtProvider.getMemberId(jwt);
            Role role = jwtProvider.getRole(jwt);

            CustomUserDetails userDetails = new CustomUserDetails(memberId, role);
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } else {
            // 토큰이 없으면 인증 없이 진행 (비회원)
            SecurityContextHolder.clearContext(); // 또는 유지
        }
        filterChain.doFilter(request, response);
    }

}

