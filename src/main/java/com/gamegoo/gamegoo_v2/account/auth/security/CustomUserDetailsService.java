package com.gamegoo.gamegoo_v2.account.auth.security;

import com.gamegoo.gamegoo_v2.account.member.repository.MemberRepository;
import com.gamegoo.gamegoo_v2.account.member.domain.Member;
import com.gamegoo.gamegoo_v2.core.exception.JwtAuthException;
import com.gamegoo.gamegoo_v2.core.exception.common.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private final MemberRepository memberRepository;

    public UserDetails loadUserByMemberId(Long memberId) throws UsernameNotFoundException {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new JwtAuthException(ErrorCode.MEMBER_NOT_FOUND));

        // 탈퇴한 회원인 경우 에러 발생
        if (member.isBlind()) {
            throw new JwtAuthException(ErrorCode.INACTIVE_MEMBER);
        }

        return new CustomUserDetails(member.getId(),"MEMBER");
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        throw new UnsupportedOperationException("loadUserByUsername(String username) is not supported. Use loadUserByMemberIdAndSocialId(Long socialId, String role) instead.");
    }
}
