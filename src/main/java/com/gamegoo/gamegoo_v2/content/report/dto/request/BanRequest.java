package com.gamegoo.gamegoo_v2.content.report.dto.request;

import com.gamegoo.gamegoo_v2.account.member.domain.BanType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BanRequest {
    private BanType banType;
    private LocalDateTime banExpireAt;
    private String banScope;
} 