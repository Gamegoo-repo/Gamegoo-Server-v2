package com.gamegoo.gamegoo_v2.core.event;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SendReportEmailEvent {

    Long reportId;
    Long fromMemberId;
    String fromMemberGameName;
    String fromMemberTag;
    Long toMemberId;

}
