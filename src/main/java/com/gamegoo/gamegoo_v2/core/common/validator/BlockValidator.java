package com.gamegoo.gamegoo_v2.core.common.validator;

import com.gamegoo.gamegoo_v2.account.member.domain.Member;
import com.gamegoo.gamegoo_v2.core.exception.common.ErrorCode;
import com.gamegoo.gamegoo_v2.core.exception.common.GlobalException;
import com.gamegoo.gamegoo_v2.social.block.repository.BlockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BlockValidator extends BaseValidator {

    private final BlockRepository blockRepository;

    /**
     * 회원이 상대 회원을 차단한 경우 입력받은 Exception을 발생시키는 메소드
     *
     * @param member         회원
     * @param targetMember   상대 회원
     * @param exceptionClass 예외 클래스
     * @param errorCode      에러 코드
     */
    public <T extends GlobalException> void throwIfBlocked(Member member, Member targetMember,
                                                           Class<T> exceptionClass, ErrorCode errorCode) {
        boolean exists = blockRepository.existsByBlockerMemberAndBlockedMemberAndDeleted(member, targetMember, false);
        if (exists) {
            throw createException(exceptionClass, errorCode);
        }
    }

}
