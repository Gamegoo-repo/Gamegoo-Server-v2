package com.gamegoo.gamegoo_v2.core.common.validator;

import com.gamegoo.gamegoo_v2.account.member.domain.Member;
import com.gamegoo.gamegoo_v2.account.member.service.BanService;
import com.gamegoo.gamegoo_v2.core.exception.MemberException;
import com.gamegoo.gamegoo_v2.core.exception.common.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BanValidator {

    private final BanService banService;

    public void throwIfBannedFromPosting(Member member) {
        banService.checkBanExpiry(member);
        if (!member.canWritePost()) {
            throw new MemberException(ErrorCode.MEMBER_BANNED_FROM_POSTING);
        }
    }

    public void throwIfBannedFromChatting(Member member) {
        banService.checkBanExpiry(member);
        if (!member.canChat()) {
            throw new MemberException(ErrorCode.MEMBER_BANNED_FROM_CHATTING);
        }
    }

    public void throwIfBannedFromMatching(Member member) {
        banService.checkBanExpiry(member);
        if (!member.canMatch()) {
            throw new MemberException(ErrorCode.MEMBER_BANNED_FROM_MATCHING);
        }
    }

    public void throwIfBanned(Member member) {
        banService.checkBanExpiry(member);
        if (member.isBanned()) {
            throw new MemberException(ErrorCode.MEMBER_BANNED);
        }
    }

    public boolean isBanned(Member member) {
        banService.checkBanExpiry(member);
        return member.isBanned();
    }
}