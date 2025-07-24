package com.gamegoo.gamegoo_v2.core.config;

import com.gamegoo.gamegoo_v2.account.member.domain.BanType;
import com.gamegoo.gamegoo_v2.account.member.domain.LoginType;
import com.gamegoo.gamegoo_v2.account.member.domain.Mike;
import com.gamegoo.gamegoo_v2.account.member.domain.Position;
import com.gamegoo.gamegoo_v2.account.member.domain.Tier;
import com.gamegoo.gamegoo_v2.content.report.domain.ReportPath;
import com.gamegoo.gamegoo_v2.content.report.domain.ReportSortOrder;
import com.gamegoo.gamegoo_v2.content.report.domain.ReportType;
import com.gamegoo.gamegoo_v2.matching.domain.GameMode;
import com.gamegoo.gamegoo_v2.matching.domain.MatchingStatus;
import com.gamegoo.gamegoo_v2.matching.domain.MatchingType;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Schema;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenApiCustomizer openApiCustomizer() {
        return openApi -> {
            Components components = openApi.getComponents();
            if (components == null) {
                components = new Components();
                openApi.setComponents(components);
            }

            // Tier enum 등록
            Schema<String> tierSchema = new Schema<String>()
                    .type("string")
                    .description("리그오브레전드 티어");
            tierSchema.setEnum(Arrays.asList(
                    Tier.UNRANKED.name(),
                    Tier.IRON.name(),
                    Tier.BRONZE.name(),
                    Tier.SILVER.name(),
                    Tier.GOLD.name(),
                    Tier.PLATINUM.name(),
                    Tier.EMERALD.name(),
                    Tier.DIAMOND.name(),
                    Tier.MASTER.name(),
                    Tier.GRANDMASTER.name(),
                    Tier.CHALLENGER.name()
            ));
            components.addSchemas("Tier", tierSchema);

            // Position enum 등록
            Schema<String> positionSchema = new Schema<String>()
                    .type("string")
                    .description("게임 포지션");
            positionSchema.setEnum(Arrays.asList(
                    Position.ANY.name(),
                    Position.TOP.name(),
                    Position.JUNGLE.name(),
                    Position.MID.name(),
                    Position.ADC.name(),
                    Position.SUP.name()
            ));
            components.addSchemas("Position", positionSchema);

            // Mike enum 등록
            Schema<String> mikeSchema = new Schema<String>()
                    .type("string")
                    .description("마이크 사용 가능 여부");
            mikeSchema.setEnum(Arrays.asList(
                    Mike.UNAVAILABLE.name(),
                    Mike.AVAILABLE.name()
            ));
            components.addSchemas("Mike", mikeSchema);

            // GameMode enum 등록
            Schema<String> gameModeSchema = new Schema<String>()
                    .type("string")
                    .description("게임 모드");
            gameModeSchema.setEnum(Arrays.asList(
                    GameMode.FAST.name(),
                    GameMode.SOLO.name(),
                    GameMode.FREE.name(),
                    GameMode.ARAM.name()
            ));
            components.addSchemas("GameMode", gameModeSchema);

            // LoginType enum 등록
            Schema<String> loginTypeSchema = new Schema<String>()
                    .type("string")
                    .description("로그인 유형");
            loginTypeSchema.setEnum(Arrays.asList(
                    LoginType.GENERAL.name(),
                    LoginType.RSO.name()
            ));
            components.addSchemas("LoginType", loginTypeSchema);

            // BanType enum 등록
            Schema<String> banTypeSchema = new Schema<String>()
                    .type("string")
                    .description("차단 유형");
            banTypeSchema.setEnum(Arrays.asList(
                    BanType.NONE.name(),
                    BanType.WARNING.name(),
                    BanType.BAN_1D.name(),
                    BanType.BAN_3D.name(),
                    BanType.BAN_5D.name(),
                    BanType.BAN_1W.name(),
                    BanType.BAN_2W.name(),
                    BanType.BAN_1M.name(),
                    BanType.PERMANENT.name()
            ));
            components.addSchemas("BanType", banTypeSchema);

            // MatchingType enum 등록
            Schema<String> matchingTypeSchema = new Schema<String>()
                    .type("string")
                    .description("매칭 타입");
            matchingTypeSchema.setEnum(Arrays.asList(
                    MatchingType.BASIC.name(),
                    MatchingType.PRECISE.name()
            ));
            components.addSchemas("MatchingType", matchingTypeSchema);

            // ReportSortOrder enum 등록
            Schema<String> reportSortOrderSchema = new Schema<String>()
                    .type("string")
                    .description("신고 정렬 순서");
            reportSortOrderSchema.setEnum(Arrays.asList(
                    ReportSortOrder.LATEST.name(),
                    ReportSortOrder.OLDEST.name()
            ));
            components.addSchemas("ReportSortOrder", reportSortOrderSchema);

            // ReportType enum 등록 (ID 기반)
            Schema<Integer> reportTypeSchema = new Schema<Integer>()
                    .type("integer")
                    .description("신고 유형 (1=스팸, 2=불법정보, 3=성희롱, 4=욕설/혐오, 5=개인정보노출, 6=불쾌한표현)");
            reportTypeSchema.setEnum(Arrays.asList(
                    ReportType.SPAM.getId(),
                    ReportType.ILLEGAL_CONTENT.getId(),
                    ReportType.HARASSMENT.getId(),
                    ReportType.HATE_SPEECH.getId(),
                    ReportType.PRIVACY_VIOLATION.getId(),
                    ReportType.OFFENSIVE.getId()
            ));
            components.addSchemas("ReportType", reportTypeSchema);

            // ReportPath enum 등록
            Schema<String> reportPathSchema = new Schema<String>()
                    .type("string")
                    .description("신고 경로");
            reportPathSchema.setEnum(Arrays.asList(
                    ReportPath.BOARD.name(),
                    ReportPath.CHAT.name(),
                    ReportPath.PROFILE.name()
            ));
            components.addSchemas("ReportPath", reportPathSchema);

            // MatchingStatus enum 등록
            Schema<String> matchingStatusSchema = new Schema<String>()
                    .type("string")
                    .description("매칭 상태");
            matchingStatusSchema.setEnum(Arrays.asList(
                    MatchingStatus.FAIL.name(),
                    MatchingStatus.SUCCESS.name(),
                    MatchingStatus.PENDING.name(),
                    MatchingStatus.FOUND.name(),
                    MatchingStatus.QUIT.name()
            ));
            components.addSchemas("MatchingStatus", matchingStatusSchema);
        };
    }
}
