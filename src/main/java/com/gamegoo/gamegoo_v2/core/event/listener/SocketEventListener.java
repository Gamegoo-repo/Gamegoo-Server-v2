package com.gamegoo.gamegoo_v2.core.event.listener;

import com.gamegoo.gamegoo_v2.core.event.SocketJoinEvent;
import com.gamegoo.gamegoo_v2.external.socket.SocketService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SocketEventListener {

    private final SocketService socketService;

    /**
     * socket 서버로 join API 요청 event listener
     *
     * @param event event
     */
    @Async
    @EventListener
    public void handleSocketJoinEvent(SocketJoinEvent event) {
        socketService.joinSocketToChatroom(event.getMemberId(), event.getUuid());
    }

}
