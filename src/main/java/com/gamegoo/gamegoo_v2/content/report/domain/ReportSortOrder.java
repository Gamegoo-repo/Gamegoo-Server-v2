package com.gamegoo.gamegoo_v2.content.report.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ReportSortOrder {
    LATEST("최신순"),
    OLDEST("오래된순");

    private final String description;
}