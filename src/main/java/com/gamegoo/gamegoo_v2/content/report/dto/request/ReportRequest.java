package com.gamegoo.gamegoo_v2.content.report.dto.request;

import com.gamegoo.gamegoo_v2.core.common.annotation.NotDuplicated;
import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ReportRequest {

    @NotDuplicated
    @NotEmpty
    List<Integer> reportCodeList;

    String contents;

    Long boardId;

}
