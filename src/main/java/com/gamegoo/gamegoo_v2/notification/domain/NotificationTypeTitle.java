package com.gamegoo.gamegoo_v2.notification.domain;

import lombok.Getter;

@Getter
public enum NotificationTypeTitle {

    FRIEND_REQUEST_SEND(1, "님에게 친구 요청을 보냈어요.", null),
    FRIEND_REQUEST_RECEIVED(1, "님에게 친구 요청이 왔어요.", "/member/profile/"),
    FRIEND_REQUEST_ACCEPTED(1, "님이 친구를 수락했어요.", null),
    FRIEND_REQUEST_REJECTED(1, "님이 친구를 거절했어요.", null),
    MANNER_LEVEL_UP(2, "매너레벨이 n단계로 올라갔어요!", "/member/manner"),
    MANNER_LEVEL_DOWN(2, "매너레벨이 n단계로 떨어졌어요.", "/member/manner"),
    MANNER_KEYWORD_RATED(3, "지난 매칭에서 n 키워드를 받았어요. 자세한 내용은 내 평가에서 확인하세요!", "/member/manner"),
    TEST_ALARM(0, "TEST PUSH. NUMBER: ", null),
    ;

    private final int imgType;
    private final String message;
    private final String sourceUrl;

    NotificationTypeTitle(int imgType, String message, String sourceUrl) {
        this.imgType = imgType;
        this.message = message;
        this.sourceUrl = sourceUrl;
    }
}
