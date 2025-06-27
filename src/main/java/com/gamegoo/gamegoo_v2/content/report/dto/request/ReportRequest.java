package com.gamegoo.gamegoo_v2.content.report.dto.request;

import com.gamegoo.gamegoo_v2.core.common.annotation.EachMax;
import com.gamegoo.gamegoo_v2.core.common.annotation.EachMin;
import com.gamegoo.gamegoo_v2.core.common.annotation.NotDuplicated;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import org.hibernate.validator.constraints.Length;

import java.util.List;

@Getter
@Builder
public class ReportRequest {

    @NotDuplicated
    @NotEmpty(message = "신고 코드 리스트는 비워둘 수 없습니다.")
    @EachMin(value = 1, message = "report code는 1 이상의 값이어야 합니다.")
    @EachMax(value = 6, message = "report code는 6 이하의 값이어야 합니다.")
    List<Integer> reportCodeList;

    @Length(max = 500, message = "contents는 500자 이내여야 합니다.")
    String contents;

    @Min(value = 1, message = "path code는 1 이상의 값이어야 합니다.")
    @Max(value = 3, message = "path code는 3 이하의 값이어야 합니다.")
    @NotNull(message = "path code는 필수 값 입니다.")
    Integer pathCode;

    Long boardId;

}
