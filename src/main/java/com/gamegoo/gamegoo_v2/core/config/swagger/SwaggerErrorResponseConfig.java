package com.gamegoo.gamegoo_v2.core.config.swagger;

import com.gamegoo.gamegoo_v2.account.auth.annotation.AuthMember;
import com.gamegoo.gamegoo_v2.core.exception.common.ErrorCode;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponses;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Configuration
public class SwaggerErrorResponseConfig {

    public static final String ERROR_RESPONSE_SCHEMA_NAME = "ApiErrorResponse";

    @Bean
    public OperationCustomizer apiErrorResponseCustomizer() {
        return (operation, handlerMethod) -> {
            ApiErrorCodes annotation = handlerMethod.getMethodAnnotation(ApiErrorCodes.class);

            Set<ErrorCode> errorCodeSet = new LinkedHashSet<>();
            if (annotation != null && annotation.value().length > 0) {
                errorCodeSet.addAll(Arrays.asList(annotation.value()));
            }

            boolean hasRequiredAuthMember = Arrays.stream(handlerMethod.getMethodParameters())
                    .anyMatch(parameter -> {
                        AuthMember authMember = parameter.getParameterAnnotation(AuthMember.class);
                        return authMember != null && authMember.required();
                    });

            if (hasRequiredAuthMember) {
                errorCodeSet.add(ErrorCode.UNAUTHORIZED_EXCEPTION);
                errorCodeSet.add(ErrorCode.MEMBER_NOT_FOUND);
                errorCodeSet.add(ErrorCode.INACTIVE_MEMBER);
            }

            if (errorCodeSet.isEmpty()) {
                return operation;
            }

            Map<HttpStatus, List<ErrorCode>> groupedErrorCodes = errorCodeSet.stream()
                    .collect(Collectors.groupingBy(ErrorCode::getStatus, LinkedHashMap::new, Collectors.toList()));

            ApiResponses responses = operation.getResponses();
            if (responses == null) {
                responses = new ApiResponses();
                operation.setResponses(responses);
            }

            final ApiResponses apiResponses = responses;

            groupedErrorCodes.forEach((status, errorCodes) -> {
                String responseCode = String.valueOf(status.value());
                io.swagger.v3.oas.models.responses.ApiResponse apiResponse = apiResponses.computeIfAbsent(
                        responseCode,
                        key -> new io.swagger.v3.oas.models.responses.ApiResponse().description(status.getReasonPhrase())
                );

                String description = errorCodes.stream()
                        .map(errorCode -> String.format("[%s] %s", errorCode.getCode(), errorCode.getMessage()))
                        .collect(Collectors.joining("\n"));

                if (apiResponse.getDescription() == null || apiResponse.getDescription().isBlank()
                        || status.getReasonPhrase().equals(apiResponse.getDescription())) {
                    apiResponse.setDescription(description);
                } else if (!apiResponse.getDescription().contains(description)) {
                    apiResponse.setDescription(apiResponse.getDescription() + "\n" + description);
                }

                Content content = apiResponse.getContent();
                if (content == null) {
                    content = new Content();
                    apiResponse.setContent(content);
                }

                MediaType mediaType = content.get(org.springframework.http.MediaType.APPLICATION_JSON_VALUE);
                if (mediaType == null) {
                    mediaType = new MediaType();
                    mediaType.schema(new Schema<>().$ref("#/components/schemas/" + ERROR_RESPONSE_SCHEMA_NAME));
                    content.addMediaType(org.springframework.http.MediaType.APPLICATION_JSON_VALUE, mediaType);
                }
            });

            return operation;
        };
    }
}
