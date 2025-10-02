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
import com.gamegoo.gamegoo_v2.core.config.swagger.SwaggerErrorResponseConfig;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;

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

            // 일반 String enum들 등록
            addStringEnumSchema(components, "Tier", Tier.class, "리그오브레전드 티어");
            addStringEnumSchema(components, "Position", Position.class, "게임 포지션");
            addStringEnumSchema(components, "Mike", Mike.class, "마이크 사용 가능 여부");
            addStringEnumSchema(components, "GameMode", GameMode.class, "게임 모드");
            addStringEnumSchema(components, "LoginType", LoginType.class, "로그인 유형");
            addStringEnumSchema(components, "BanType", BanType.class, "차단 유형");
            addStringEnumSchema(components, "MatchingType", MatchingType.class, "매칭 타입");
            addStringEnumSchema(components, "ReportSortOrder", ReportSortOrder.class, "신고 정렬 순서");
            addStringEnumSchema(components, "ReportPath", ReportPath.class, "신고 경로");
            addStringEnumSchema(components, "MatchingStatus", MatchingStatus.class, "매칭 상태");

            // ReportType은 특별 처리 (ID 기반)
            addReportTypeSchema(components);

            addErrorResponseSchema(components);
        };
    }

    /**
     * 일반적인 String enum 스키마를 등록하는 헬퍼 메서드
     */
    private <T extends Enum<T>> void addStringEnumSchema(Components components, String schemaName, 
                                                        Class<T> enumClass, String description) {
        Schema<String> schema = new Schema<String>()
                .type("string")
                .description(description);
        
        List<String> enumValues = Arrays.stream(enumClass.getEnumConstants())
                .map(Enum::name)
                .toList();
        
        schema.setEnum(enumValues);
        components.addSchemas(schemaName, schema);
    }

    /**
     * ReportType 전용 스키마 등록 (ID 기반)
     */
    private void addReportTypeSchema(Components components) {
        Schema<Integer> reportTypeSchema = new Schema<Integer>()
                .type("integer")
                .description("신고 유형 (1=스팸, 2=불법정보, 3=성희롱, 4=욕설/혐오, 5=개인정보노출, 6=불쾌한표현)");

        List<Integer> enumValues = Arrays.stream(ReportType.values())
                .map(ReportType::getId)
                .toList();

        reportTypeSchema.setEnum(enumValues);
        components.addSchemas("ReportType", reportTypeSchema);
    }

    private void addErrorResponseSchema(Components components) {
        if (components.getSchemas() != null
                && components.getSchemas().containsKey(SwaggerErrorResponseConfig.ERROR_RESPONSE_SCHEMA_NAME)) {
            return;
        }

        ObjectSchema errorResponseSchema = new ObjectSchema();
        errorResponseSchema.setDescription("공통 에러 응답 포맷");

        errorResponseSchema.addProperties("status", new IntegerSchema()
                .description("HTTP 상태 코드")
                .example(400));
        errorResponseSchema.addProperties("message", new StringSchema()
                .description("에러 메시지")
                .example("잘못된 요청입니다."));
        errorResponseSchema.addProperties("code", new StringSchema()
                .description("비즈니스 에러 코드")
                .example("COMMON400"));

        errorResponseSchema.addProperties("data", new Schema<>()
                .type("object")
                .nullable(true)
                .description("응답 데이터 (에러 시 null)"));

        components.addSchemas(SwaggerErrorResponseConfig.ERROR_RESPONSE_SCHEMA_NAME, errorResponseSchema);
    }
}
