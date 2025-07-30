package com.gamegoo.gamegoo_v2.account.auth.annotation.resolver;

import com.gamegoo.gamegoo_v2.account.auth.annotation.AuthMember;
import com.gamegoo.gamegoo_v2.account.auth.security.SecurityUtil;
import com.gamegoo.gamegoo_v2.account.member.domain.Member;
import com.gamegoo.gamegoo_v2.account.member.repository.MemberRepository;
import com.gamegoo.gamegoo_v2.core.exception.AuthException;
import com.gamegoo.gamegoo_v2.core.exception.MemberException;
import com.gamegoo.gamegoo_v2.core.exception.common.ErrorCode;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Component
@RequiredArgsConstructor
public class AuthMemberArgumentResolver implements HandlerMethodArgumentResolver {

    private final MemberRepository memberRepository;

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        // @AuthMember 어노테이션이 붙어 있는지 여부
        boolean hasParameterAnnotation = parameter.hasParameterAnnotation(AuthMember.class);

        // 파라미터 타입이 Member 클래스인지 여부
        boolean hasMemberClass = Member.class.isAssignableFrom(parameter.getParameterType());

        return hasParameterAnnotation && hasMemberClass;
    }

    @Override
    public Object resolveArgument(@NonNull MethodParameter parameter, ModelAndViewContainer mavContainer,
                                  @NonNull NativeWebRequest webRequest,
                                  WebDataBinderFactory binderFactory) {
        AuthMember authMember = parameter.getParameterAnnotation(AuthMember.class);
        boolean required = authMember == null || authMember.required(); // 기본값 true

        Long currentMemberId = SecurityUtil.getCurrentMemberId();
        if (currentMemberId != null) {
            return memberRepository.findById(currentMemberId)
                    .map(member -> {
                        if (member.getBlind()) {
                            throw new MemberException(ErrorCode.INACTIVE_MEMBER); // 탈퇴 여부 검증
                        }
                        return member;
                    })
                    .orElseThrow(() -> new MemberException(ErrorCode.MEMBER_NOT_FOUND));
        }

        if (required) {
            throw new AuthException(ErrorCode.UNAUTHORIZED_EXCEPTION);
        }

        return null; // 비회원 요청인 경우 null 객체 리턴

    }

}
