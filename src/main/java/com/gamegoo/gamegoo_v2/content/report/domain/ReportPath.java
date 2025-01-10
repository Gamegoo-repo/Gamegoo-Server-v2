package com.gamegoo.gamegoo_v2.content.report.domain;

import com.gamegoo.gamegoo_v2.core.exception.ReportException;
import com.gamegoo.gamegoo_v2.core.exception.common.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
@AllArgsConstructor
public enum ReportPath {
    BOARD(1),
    CHAT(2),
    PROFILE(3);

    private final int id;

    // id로 reportPath 객체 조회하기 위한 map
    private static final Map<Integer, ReportPath> REPORT_PATH_MAP = Arrays.stream(values())
            .collect(Collectors.toUnmodifiableMap(ReportPath::getId, report -> report));

    /**
     * id에 해당하는 ReportPath Enum을 리턴하는 메소드
     *
     * @param id id
     * @return reportPath
     */
    public static ReportPath of(Integer id) {
        if (id == null) {
            return null;
        }

        ReportPath reportPath = REPORT_PATH_MAP.get(id);
        if (reportPath == null) {
            throw new ReportException(ErrorCode.REPORT_PATH_NOT_FOUND);
        }
        return reportPath;
    }
}
