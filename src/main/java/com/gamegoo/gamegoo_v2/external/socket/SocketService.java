package com.gamegoo.gamegoo_v2.external.socket;

import com.gamegoo.gamegoo_v2.core.exception.SocketException;
import com.gamegoo.gamegoo_v2.core.exception.common.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class SocketService {

    private final RestTemplate restTemplate;

    @Value("${socket.server.url}")
    private String SOCKET_SERVER_URL;

    private static final String JOIN_CHATROOM_URL = "/socket/room/join";

    /**
     * SOCKET서버로 해당 회원의 socket을 room에 join 요청하는 API 전송
     *
     * @param memberId 회원 id
     * @param uuid     채팅방 uuid
     */
    public void joinSocketToChatroom(Long memberId, String uuid) {

        String url = SOCKET_SERVER_URL + JOIN_CHATROOM_URL;
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("memberId", memberId);
        requestBody.put("chatroomUuid", uuid);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, requestBody, String.class);

            log.info("response of joinSocketToChatroom: {}", response.getStatusCode());
            if (!response.getStatusCode().equals(HttpStatus.OK)) {
                log.error("joinSocketToChatroom API call FAIL: {}", response.getBody());
                throw new SocketException(ErrorCode.SOCKET_API_RESPONSE_ERROR);
            } else {
                log.info("joinSocketToChatroom API call SUCCESS: {}", response.getBody());
            }
        } catch (Exception e) {
            log.error("Error occurred while joinSocketToChatroom method", e);
            throw new SocketException(ErrorCode.SOCKET_API_RESPONSE_ERROR);
        }
    }

}
