package com.gamegoo.gamegoo_v2.controller.manner;

import com.gamegoo.gamegoo_v2.account.member.domain.Member;
import com.gamegoo.gamegoo_v2.controller.ControllerTestSupport;
import com.gamegoo.gamegoo_v2.controller.WithCustomMockMember;
import com.gamegoo.gamegoo_v2.social.manner.controller.MannerController;
import com.gamegoo.gamegoo_v2.social.manner.dto.request.MannerInsertRequest;
import com.gamegoo.gamegoo_v2.social.manner.dto.request.MannerUpdateRequest;
import com.gamegoo.gamegoo_v2.social.manner.dto.response.MannerInsertResponse;
import com.gamegoo.gamegoo_v2.social.manner.dto.response.MannerKeywordListResponse;
import com.gamegoo.gamegoo_v2.social.manner.dto.response.MannerRatingResponse;
import com.gamegoo.gamegoo_v2.social.manner.dto.response.MannerResponse;
import com.gamegoo.gamegoo_v2.social.manner.dto.response.MannerUpdateResponse;
import com.gamegoo.gamegoo_v2.social.manner.service.MannerFacadeService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MannerController.class)
@WithCustomMockMember
public class MannerControllerTest extends ControllerTestSupport {

    @MockitoBean
    private MannerFacadeService mannerFacadeService;

    private static final String API_URL_PREFIX = "/api/v2/manner";
    private static final Long TARGET_MEMBER_ID = 2L;

    @Nested
    @DisplayName("매너 평가 등록")
    class AddPositiveMannerRatingTest {

        @DisplayName("실패: 매너 키워드 리스트가 빈 리스트인 경우 에러 응답을 반환한다.")
        @Test
        void addPositiveMannerRatingFailedWhenKeywordListIsEmpty() throws Exception {
            // given
            List<Long> mannerKeywordIds = List.of();

            MannerInsertRequest request = MannerInsertRequest.builder()
                    .mannerKeywordIdList(mannerKeywordIds)
                    .build();

            MannerInsertResponse response = MannerInsertResponse.builder()
                    .targetMemberId(TARGET_MEMBER_ID)
                    .mannerRatingId(1L)
                    .mannerKeywordIdList(mannerKeywordIds)
                    .build();

            given(mannerFacadeService.insertPositiveMannerRating(any(Member.class), eq(TARGET_MEMBER_ID),
                    any(MannerInsertRequest.class))).willReturn(response);

            // when // then
            mockMvc.perform(post(API_URL_PREFIX + "/positive/{memberId}", TARGET_MEMBER_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("VALID_ERROR"))
                    .andExpect(jsonPath("$.message").value("매너 키워드 리스트는 비워둘 수 없습니다."));
        }

        @DisplayName("실패: 매너 키워드 리스트에 중복 값이 있는 경우 에러 응답을 반환한다.")
        @Test
        void addPositiveMannerRatingFailedWhenKeywordListIsDuplicated() throws Exception {
            // given
            List<Long> mannerKeywordIds = List.of(1L, 1L);

            MannerInsertRequest request = MannerInsertRequest.builder()
                    .mannerKeywordIdList(mannerKeywordIds)
                    .build();

            MannerInsertResponse response = MannerInsertResponse.builder()
                    .targetMemberId(TARGET_MEMBER_ID)
                    .mannerRatingId(1L)
                    .mannerKeywordIdList(mannerKeywordIds)
                    .build();

            given(mannerFacadeService.insertPositiveMannerRating(any(Member.class), eq(TARGET_MEMBER_ID),
                    any(MannerInsertRequest.class))).willReturn(response);

            // when // then
            mockMvc.perform(post(API_URL_PREFIX + "/positive/{memberId}", TARGET_MEMBER_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("VALID_ERROR"))
                    .andExpect(jsonPath("$.message").value("중복된 값을 포함할 수 없습니다."));
        }

        @DisplayName("성공")
        @Test
        void addPositiveMannerRatingSucceeds() throws Exception {
            // given
            List<Long> mannerKeywordIds = List.of(1L, 2L, 3L);

            MannerInsertRequest request = MannerInsertRequest.builder()
                    .mannerKeywordIdList(mannerKeywordIds)
                    .build();

            MannerInsertResponse response = MannerInsertResponse.builder()
                    .targetMemberId(TARGET_MEMBER_ID)
                    .mannerRatingId(1L)
                    .mannerKeywordIdList(mannerKeywordIds)
                    .build();

            given(mannerFacadeService.insertPositiveMannerRating(any(Member.class), eq(TARGET_MEMBER_ID),
                    any(MannerInsertRequest.class))).willReturn(response);

            // when // then
            mockMvc.perform(post(API_URL_PREFIX + "/positive/{memberId}", TARGET_MEMBER_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("OK"))
                    .andExpect(jsonPath("$.data.targetMemberId").value(TARGET_MEMBER_ID))
                    .andExpect(jsonPath("$.data.mannerRatingId").value(1L))
                    .andExpect(jsonPath("$.data.mannerKeywordIdList").isArray());
        }

    }

    @Nested
    @DisplayName("비매너 평가 등록")
    class AddNegativeMannerRatingTest {

        @DisplayName("실패: 매너 키워드 리스트가 빈 리스트인 경우 에러 응답을 반환한다.")
        @Test
        void addNegativeMannerRatingFailedWhenKeywordListIsEmpty() throws Exception {
            // given
            List<Long> mannerKeywordIds = List.of();

            MannerInsertRequest request = MannerInsertRequest.builder()
                    .mannerKeywordIdList(mannerKeywordIds)
                    .build();

            MannerInsertResponse response = MannerInsertResponse.builder()
                    .targetMemberId(TARGET_MEMBER_ID)
                    .mannerRatingId(1L)
                    .mannerKeywordIdList(mannerKeywordIds)
                    .build();

            given(mannerFacadeService.insertNegativeMannerRating(any(Member.class), eq(TARGET_MEMBER_ID),
                    any(MannerInsertRequest.class))).willReturn(response);

            // when // then
            mockMvc.perform(post(API_URL_PREFIX + "/negative/{memberId}", TARGET_MEMBER_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("VALID_ERROR"))
                    .andExpect(jsonPath("$.message").value("매너 키워드 리스트는 비워둘 수 없습니다."));
        }

        @DisplayName("실패: 매너 키워드 리스트에 중복 값이 있는 경우 에러 응답을 반환한다.")
        @Test
        void addNegativeMannerRatingFailedWhenKeywordListIsDuplicated() throws Exception {
            // given
            List<Long> mannerKeywordIds = List.of(7L, 7L);

            MannerInsertRequest request = MannerInsertRequest.builder()
                    .mannerKeywordIdList(mannerKeywordIds)
                    .build();

            MannerInsertResponse response = MannerInsertResponse.builder()
                    .targetMemberId(TARGET_MEMBER_ID)
                    .mannerRatingId(1L)
                    .mannerKeywordIdList(mannerKeywordIds)
                    .build();

            given(mannerFacadeService.insertNegativeMannerRating(any(Member.class), eq(TARGET_MEMBER_ID),
                    any(MannerInsertRequest.class))).willReturn(response);

            // when // then
            mockMvc.perform(post(API_URL_PREFIX + "/negative/{memberId}", TARGET_MEMBER_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("VALID_ERROR"))
                    .andExpect(jsonPath("$.message").value("중복된 값을 포함할 수 없습니다."));
        }

        @DisplayName("성공")
        @Test
        void addNegativeMannerRatingSucceeds() throws Exception {
            // given
            List<Long> mannerKeywordIds = List.of(7L, 8L, 9L);

            MannerInsertRequest request = MannerInsertRequest.builder()
                    .mannerKeywordIdList(mannerKeywordIds)
                    .build();

            MannerInsertResponse response = MannerInsertResponse.builder()
                    .targetMemberId(TARGET_MEMBER_ID)
                    .mannerRatingId(1L)
                    .mannerKeywordIdList(mannerKeywordIds)
                    .build();

            given(mannerFacadeService.insertNegativeMannerRating(any(Member.class), eq(TARGET_MEMBER_ID),
                    any(MannerInsertRequest.class))).willReturn(response);

            // when // then
            mockMvc.perform(post(API_URL_PREFIX + "/negative/{memberId}", TARGET_MEMBER_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("OK"))
                    .andExpect(jsonPath("$.data.targetMemberId").value(TARGET_MEMBER_ID))
                    .andExpect(jsonPath("$.data.mannerRatingId").value(1L))
                    .andExpect(jsonPath("$.data.mannerKeywordIdList").isArray());
        }

    }

    @Nested
    @DisplayName("매너/비매너 평가 수정")
    class UpdateMannerRatingTest {

        @DisplayName("실패: 매너 키워드 리스트가 빈 리스트인 경우 에러 응답을 반환한다.")
        @Test
        void updateMannerRatingFailedWhenKeywordListIsEmpty() throws Exception {
            // given
            List<Long> mannerKeywordIds = List.of();

            MannerUpdateRequest request = MannerUpdateRequest.builder()
                    .mannerKeywordIdList(mannerKeywordIds)
                    .build();

            MannerUpdateResponse response = MannerUpdateResponse.builder()
                    .targetMemberId(TARGET_MEMBER_ID)
                    .mannerRatingId(1L)
                    .mannerKeywordIdList(mannerKeywordIds)
                    .build();

            given(mannerFacadeService.updateMannerRating(any(Member.class), eq(1L),
                    any(MannerUpdateRequest.class))).willReturn(response);

            // when // then
            mockMvc.perform(put(API_URL_PREFIX + "/{mannerId}", 1L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("VALID_ERROR"))
                    .andExpect(jsonPath("$.message").value("매너 키워드 리스트는 비워둘 수 없습니다."));
        }

        @DisplayName("실패: 매너 키워드 리스트에 중복 값이 있는 경우 에러 응답을 반환한다.")
        @Test
        void updateMannerRatingFailedWhenKeywordListIsDuplicated() throws Exception {
            // given
            List<Long> mannerKeywordIds = List.of(1L, 1L);

            MannerUpdateRequest request = MannerUpdateRequest.builder()
                    .mannerKeywordIdList(mannerKeywordIds)
                    .build();

            MannerUpdateResponse response = MannerUpdateResponse.builder()
                    .targetMemberId(TARGET_MEMBER_ID)
                    .mannerRatingId(1L)
                    .mannerKeywordIdList(mannerKeywordIds)
                    .build();

            given(mannerFacadeService.updateMannerRating(any(Member.class), eq(1L),
                    any(MannerUpdateRequest.class))).willReturn(response);

            // when // then
            mockMvc.perform(put(API_URL_PREFIX + "/{mannerId}", 1L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("VALID_ERROR"))
                    .andExpect(jsonPath("$.message").value("중복된 값을 포함할 수 없습니다."));
        }

        @DisplayName("성공")
        @Test
        void updateMannerRatingSucceeds() throws Exception {
            // given
            List<Long> mannerKeywordIds = List.of(1L, 2L, 3L);

            MannerUpdateRequest request = MannerUpdateRequest.builder()
                    .mannerKeywordIdList(mannerKeywordIds)
                    .build();

            MannerUpdateResponse response = MannerUpdateResponse.builder()
                    .targetMemberId(TARGET_MEMBER_ID)
                    .mannerRatingId(1L)
                    .mannerKeywordIdList(mannerKeywordIds)
                    .build();

            given(mannerFacadeService.updateMannerRating(any(Member.class), eq(1L),
                    any(MannerUpdateRequest.class))).willReturn(response);

            // when // then
            mockMvc.perform(put(API_URL_PREFIX + "/{mannerId}", 1L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("OK"))
                    .andExpect(jsonPath("$.data.targetMemberId").value(TARGET_MEMBER_ID))
                    .andExpect(jsonPath("$.data.mannerRatingId").value(1L))
                    .andExpect(jsonPath("$.data.mannerKeywordIdList").isArray());
        }

    }

    @DisplayName("특정 회원에 대한 나의 매너 평가 조회")
    @Test
    void getPositiveMannerRatingInfoSucceeds() throws Exception {
        // given
        MannerRatingResponse response = MannerRatingResponse.builder()
                .mannerRatingId(1L)
                .mannerKeywordIdList(List.of(1L, 2L, 3L))
                .build();

        given(mannerFacadeService.getMannerRating(any(Member.class), eq(TARGET_MEMBER_ID), eq(true)))
                .willReturn(response);

        // when // then
        mockMvc.perform(get(API_URL_PREFIX + "/positive/{memberId}", TARGET_MEMBER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("OK"))
                .andExpect(jsonPath("$.data.mannerRatingId").value(1L))
                .andExpect(jsonPath("$.data.mannerKeywordIdList").isArray());
    }

    @DisplayName("특정 회원에 대한 나의 비매너 평가 조회")
    @Test
    void getNegativeMannerRatingInfoSucceeds() throws Exception {
        // given
        MannerRatingResponse response = MannerRatingResponse.builder()
                .mannerRatingId(1L)
                .mannerKeywordIdList(List.of(7L, 8L, 9L))
                .build();

        given(mannerFacadeService.getMannerRating(any(Member.class), eq(TARGET_MEMBER_ID), eq(false)))
                .willReturn(response);

        // when // then
        mockMvc.perform(get(API_URL_PREFIX + "/negative/{memberId}", TARGET_MEMBER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("OK"))
                .andExpect(jsonPath("$.data.mannerRatingId").value(1L))
                .andExpect(jsonPath("$.data.mannerKeywordIdList").isArray());
    }

    @DisplayName("특정 회원의 매너 레벨 정보 조회")
    @Test
    void getMannerLevelInfoSucceeds() throws Exception {
        // given
        MannerResponse response = MannerResponse.of(1, 50.0, 2);

        given(mannerFacadeService.getMannerLevelInfo(TARGET_MEMBER_ID)).willReturn(response);

        // when // then
        mockMvc.perform(get(API_URL_PREFIX + "/level/{memberId}", TARGET_MEMBER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("OK"))
                .andExpect(jsonPath("$.data.mannerLevel").value(1))
                .andExpect(jsonPath("$.data.mannerRank").value(50.0))
                .andExpect(jsonPath("$.data.mannerRatingCount").value(2));
    }

    @DisplayName("특정 회원의 매너 키워드 정보 조회")
    @Test
    void getMannerKeywordInfoSucceeds() throws Exception {
        // given
        MannerKeywordListResponse response = MannerKeywordListResponse.builder()
                .mannerKeywords(List.of())
                .build();

        given(mannerFacadeService.getMannerKeywordInfo(TARGET_MEMBER_ID)).willReturn(response);

        // when // then
        mockMvc.perform(get(API_URL_PREFIX + "/keyword/{memberId}", TARGET_MEMBER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("OK"))
                .andExpect(jsonPath("$.data.mannerKeywords").isArray());
    }

}
