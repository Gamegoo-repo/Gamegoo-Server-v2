package com.gamegoo.gamegoo_v2.common.validator;

import com.gamegoo.gamegoo_v2.exception.MemberException;
import com.gamegoo.gamegoo_v2.exception.common.ErrorCode;
import com.gamegoo.gamegoo_v2.member.domain.Member;
import org.springframework.stereotype.Component;

@Component
public class MemberValidator {

    /**
     * 대상 회원이 탈퇴하지 않았는지 검증
     *
     * @param member
     */
    public void validateTargetMemberIsNotBlind(Member member) {
        if (member.isBlind()) {
            throw new MemberException(ErrorCode.TARGET_MEMBER_DEACTIVATED);
        }
    }


}
