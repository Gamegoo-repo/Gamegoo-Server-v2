package com.gamegoo.gamegoo_v2.controller.notification;

import com.gamegoo.gamegoo_v2.account.member.domain.Member;
import com.gamegoo.gamegoo_v2.controller.ControllerTestSupport;
import com.gamegoo.gamegoo_v2.controller.WithCustomMockMember;
import com.gamegoo.gamegoo_v2.core.exception.NotificationException;
import com.gamegoo.gamegoo_v2.core.exception.common.ErrorCode;
import com.gamegoo.gamegoo_v2.notification.controller.NotificationController;
import com.gamegoo.gamegoo_v2.notification.domain.Notification;
import com.gamegoo.gamegoo_v2.notification.dto.NotificationCursorListResponse;
import com.gamegoo.gamegoo_v2.notification.dto.NotificationPageListResponse;
import com.gamegoo.gamegoo_v2.notification.dto.ReadNotificationResponse;
import com.gamegoo.gamegoo_v2.notification.service.NotificationFacadeService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(NotificationController.class)
@WithCustomMockMember
class NotificationControllerTest extends ControllerTestSupport {

    @MockitoBean
    private NotificationFacadeService notificationFacadeService;

    private static final String API_URL_PREFIX = "/api/v2/notification";
    private static final Long NOTIFICATION_ID = 1L;

    @Nested
    @DisplayName("알림 읽음 처리")
    class ReadNotificationTest {

        @DisplayName("알림 읽음 처리 성공")
        @Test
        void readNotificationSucceeds() throws Exception {
            // given
            ReadNotificationResponse response = ReadNotificationResponse.of(NOTIFICATION_ID);

            given(notificationFacadeService.readNotification(any(Member.class), any(Long.class))).willReturn(response);

            // when // then
            mockMvc.perform(patch(API_URL_PREFIX + "/{notificationId}", NOTIFICATION_ID))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("OK"))
                    .andExpect(jsonPath("$.data.message").value("알림 읽음 처리 성공"))
                    .andExpect(jsonPath("$.data.notificationId").value(NOTIFICATION_ID));

        }

        @DisplayName("알림 읽음 처리 실패: id에 해당하는 알림이 없는 경우 에러 응답을 반환한다.")
        @Test
        void readNotificationFailedWhenNotificationNotExists() throws Exception {
            // given
            willThrow(new NotificationException(ErrorCode.NOTIFICATION_NOT_FOUND))
                    .given(notificationFacadeService).readNotification(any(Member.class), any(Long.class));

            // when // then
            mockMvc.perform(patch(API_URL_PREFIX + "/{notificationId}", NOTIFICATION_ID))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value(ErrorCode.NOTIFICATION_NOT_FOUND.getMessage()));
        }

    }

    @Nested
    @DisplayName("안읽은 알림 개수 조회")
    class countUnreadNotificationTest {

        @DisplayName("안읽은 알림 개수 조회 성공")
        @Test
        void countUnreadNotificationSucceeds() throws Exception {
            // given
            given(notificationFacadeService.countUnreadNotification(any(Member.class))).willReturn(5);

            // when // then
            mockMvc.perform(get(API_URL_PREFIX + "/unread/count"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("OK"))
                    .andExpect(jsonPath("$.data").value(5));
        }

    }

    @Nested
    @DisplayName("알림 전체 목록 조회")
    class GetNotificationPageListTest {

        @DisplayName("알림 전체 목록 조회 성공")
        @Test
        void getNotificationPageListSucceeds() throws Exception {
            // given
            NotificationPageListResponse response = NotificationPageListResponse.builder()
                    .notificationList(new ArrayList<>())
                    .listSize(0)
                    .totalPage(0)
                    .totalElements(0)
                    .isFirst(true)
                    .isLast(true)
                    .build();

            given(notificationFacadeService.getNotificationPageList(any(Member.class), any(Integer.class))).willReturn(
                    response);

            // when // then
            int pageIdx = 1;
            mockMvc.perform(get(API_URL_PREFIX + "/total")
                            .param("page", String.valueOf(pageIdx)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("OK"))
                    .andExpect(jsonPath("$.data.notificationList").isEmpty())
                    .andExpect(jsonPath("$.data.listSize").value(0))
                    .andExpect(jsonPath("$.data.totalPage").value(0))
                    .andExpect(jsonPath("$.data.totalElements").value(0))
                    .andExpect(jsonPath("$.data.isFirst").value(true))
                    .andExpect(jsonPath("$.data.isLast").value(true));
        }

        @DisplayName("알림 전체 목록 조회 실패: 페이지 번호가 1 미만인 경우 에러 응답을 반환한다")
        @Test
        void getNotificationPageListFailedWhenPageIsNotValid() throws Exception {
            // when // then
            int pageIdx = 0;
            mockMvc.perform(get(API_URL_PREFIX + "/total")
                            .param("page", String.valueOf(pageIdx)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("페이지 번호는 1 이상의 값이어야 합니다."));
        }

    }

    @Nested
    @DisplayName("알림 팝업 목록 조회")
    class GetNotificationCursorListTest {

        @DisplayName("알림 팝업 목록 조회 성공: cursor가 없는 경우")
        @Test
        void getNotificationCursorListSucceedsWhenNoCursor() throws Exception {
            // given
            List<Notification> notifications = new ArrayList<>();
            Slice<Notification> notificationSlice = new SliceImpl<>(notifications, Pageable.unpaged(), false);
            NotificationCursorListResponse response = NotificationCursorListResponse.of(notificationSlice);

            given(notificationFacadeService.getNotificationCursorList(any(Member.class), any())).willReturn(response);

            // when // then
            mockMvc.perform(get(API_URL_PREFIX))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("OK"))
                    .andExpect(jsonPath("$.data.notificationList").isArray())
                    .andExpect(jsonPath("$.data.listSize").isNumber())
                    .andExpect(jsonPath("$.data.hasNext").isBoolean());
        }

        @DisplayName("알림 팝업 목록 조회 성공: cursor가 있는 경우")
        @Test
        void getNotificationCursorListSucceedsWithCursor() throws Exception {
            // given
            List<Notification> notifications = new ArrayList<>();
            Slice<Notification> notificationSlice = new SliceImpl<>(notifications, Pageable.unpaged(), false);
            NotificationCursorListResponse response = NotificationCursorListResponse.of(notificationSlice);

            given(notificationFacadeService.getNotificationCursorList(any(Member.class), any())).willReturn(response);

            // when // then
            mockMvc.perform(get(API_URL_PREFIX)
                            .param("cursor", "1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("OK"))
                    .andExpect(jsonPath("$.data.notificationList").isArray())
                    .andExpect(jsonPath("$.data.listSize").isNumber())
                    .andExpect(jsonPath("$.data.hasNext").isBoolean());
        }

        @DisplayName("알림 팝업 목록 조회 실패: cursor가 음수인 경우 에러 응답을 반환한다.")
        @Test
        void getNotificationCursorListFailedWhenNegativeCursor() throws Exception {
            // when // then
            mockMvc.perform(get(API_URL_PREFIX)
                            .param("cursor", "-1"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("커서는 1 이상의 값이어야 합니다."));
        }

    }

}
