package com.gamegoo.gamegoo_v2.account.member.service;

import com.gamegoo.gamegoo_v2.account.member.domain.BanType;
import com.gamegoo.gamegoo_v2.account.member.domain.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BanService {

    @Transactional
    public void applyBan(Member member, BanType banType) {
        LocalDateTime banExpireAt = calculateBanExpireAt(banType);
        member.applyBan(banType, banExpireAt);
    }

    @Transactional
    public void releaseBan(Member member) {
        member.releaseBan();
    }

    public boolean isBanned(Member member) {
        return member.isBanned();
    }

    public void checkBanExpiry(Member member) {
        if (member.getBanType() != BanType.NONE && 
            member.getBanType() != BanType.PERMANENT && 
            member.getBanExpireAt() != null && 
            LocalDateTime.now().isAfter(member.getBanExpireAt())) {
            member.releaseBan();
        }
    }

    /**
     * BanType을 한국어 제재 설명으로 변환하는 메소드
     *
     * @param banType 제재 유형
     * @return 한국어 제재 설명
     */
    public String getBanReasonMessage(BanType banType) {
        if (banType == null) {
            return "제재 없음";
        }
        
        return switch (banType) {
            case WARNING -> "경고";
            case BAN_1D -> "1일 정지";
            case BAN_3D -> "3일 정지";
            case BAN_5D -> "5일 정지";
            case BAN_7D -> "7일 정지";
            case BAN_1W -> "1주 정지";
            case BAN_2W -> "2주 정지";
            case BAN_1M -> "한달 정지";
            case PERMANENT -> "영구 정지";
            default -> "제재 없음";
        };
    }

    private LocalDateTime calculateBanExpireAt(BanType banType) {
        LocalDateTime now = LocalDateTime.now();
        
        return switch (banType) {
            case WARNING, NONE -> null;
            case BAN_1D -> now.plusDays(1);
            case BAN_3D -> now.plusDays(3);
            case BAN_5D -> now.plusDays(5);
            case BAN_7D -> now.plusDays(7);
            case BAN_1W -> now.plusWeeks(1);
            case BAN_2W -> now.plusWeeks(2);
            case BAN_1M -> now.plusMonths(1);
            case PERMANENT -> null;
        };
    }
}