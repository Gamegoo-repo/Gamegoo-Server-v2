package com.gamegoo.gamegoo_v2.account.auth.service;

import com.gamegoo.gamegoo_v2.account.member.domain.Member;
import com.gamegoo.gamegoo_v2.core.exception.MemberException;
import com.gamegoo.gamegoo_v2.core.exception.common.ErrorCode;
import com.gamegoo.gamegoo_v2.utils.PasswordUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PasswordService {

    /**
     * 비밀번호 변경
     *
     * @param member      회원
     * @param newPassword 새로운 비밀번호
     */
    @Transactional
    public void changePassword(Member member, String newPassword) {
        member.updatePassword(PasswordUtil.encodePassword(newPassword));
    }

    /**
     * 비밀번호 확인
     *
     * @param member   회원
     * @param password 확인할 비밀번호
     * @return isTrue(비밀번호가 맞는지)
     */
    public boolean checkPassword(Member member, String password) {
        return PasswordUtil.matchesPassword(password, member.getPassword());
    }

    /**
     * 비밀번호 검증
     *
     * @param member   회원
     * @param password 비밀번호
     */
    public void verifyPassword(Member member, String password) {
        if (!PasswordUtil.matchesPassword(password, member.getPassword())) {
            throw new MemberException(ErrorCode.INVALID_PASSWORD);
        }
    }

    /**
     * 비밀번호 원본 검증
     *
     * @param member
     * @param rawPassword
     */
    public void verifyRawPassword(Member member, String rawPassword) {
        if (!member.getPassword().equals(rawPassword)) {
            throw new MemberException(ErrorCode.INVALID_PASSWORD);
        }
    }

}
