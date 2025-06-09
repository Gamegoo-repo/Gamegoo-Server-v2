package com.gamegoo.gamegoo_v2.core.common.validator;

import com.gamegoo.gamegoo_v2.account.member.domain.Member;
import com.gamegoo.gamegoo_v2.core.exception.MemberException;
import com.gamegoo.gamegoo_v2.core.exception.common.ErrorCode;
import com.gamegoo.gamegoo_v2.core.exception.common.GlobalException;
import org.springframework.stereotype.Component;

@Component
public class MemberValidator extends BaseValidator {

    /**
     * 대상 회원이 탈퇴한 경우 예외 발생
     *
     * @param member 회원
     */
    public void throwIfBlind(Member member) {
        if (member.getBlind()) {
            throw new MemberException(ErrorCode.TARGET_MEMBER_DEACTIVATED);
        }
    }

    /**
     * 두 회원이 서로 같은 경우 예외 발생
     *
     * @param member1 회원
     * @param member2 회원
     */
    public void throwIfEqual(Member member1, Member member2) {
        if (member1.getId().equals(member2.getId())) {
            throw new GlobalException(ErrorCode._BAD_REQUEST);
        }
    }

    /**
     * 해당 회원이 탈퇴한 상태인 경우 입력받은 Exception을 발생시키는 메소드
     *
     * @param member         회원
     * @param exceptionClass 예외 클래스
     * @param errorCode      에러 코드
     */
    public <T extends GlobalException> void throwIfBlind(Member member, Class<T> exceptionClass, ErrorCode errorCode) {
        if (member.getBlind()) {
            throw createException(exceptionClass, errorCode);
        }
    }


}
