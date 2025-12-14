package com.gamegoo.gamegoo_v2.account.member.domain;

public enum BanType {
    NONE,      // 제재 없음
    WARNING,   // 경고
    BAN_1D,    // 1일 제한
    BAN_3D,    // 3일 제한
    BAN_5D,    // 5일 제한
    BAN_1W,    // 1주 제한
    BAN_2W,    // 2주 제한
    BAN_1M,    // 한달 제한
    PERMANENT  // 영구 제한
}
