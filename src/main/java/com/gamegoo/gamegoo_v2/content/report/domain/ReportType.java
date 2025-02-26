package com.gamegoo.gamegoo_v2.content.report.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;


@Getter
@AllArgsConstructor
public enum ReportType {
    SPAM(1, "스팸 홍보/도배글"),
    ILLEGAL_CONTENT(2, "불법 정보 포함"),
    HARASSMENT(3, "성희롱 발언"),
    HATE_SPEECH(4, "욕설/ 혐오/ 차별적 표현"),
    PRIVACY_VIOLATION(5, "개인 정보 노출"),
    OFFENSIVE(6, "불쾌한 표현");

    private final int id;
    private final String description;

    // id로 ReportType 객체 조회하기 위한 map
    private static final Map<Integer, ReportType> REPORT_TYPE_MAP = Arrays.stream(values())
            .collect(Collectors.toUnmodifiableMap(ReportType::getId, report -> report));

    /**
     * id에 해당하는 ReportType Enum을 리턴하는 메소드
     *
     * @param id
     * @return
     */
    public static ReportType of(int id) {
        ReportType reportType = REPORT_TYPE_MAP.get(id);
        if (reportType == null) {
            throw new IllegalArgumentException("Invalid id: " + id);
        }
        return reportType;
    }
}
