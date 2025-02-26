package com.gamegoo.gamegoo_v2.chat.repository;

import com.gamegoo.gamegoo_v2.chat.domain.Chatroom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ChatroomRepository extends JpaRepository<Chatroom, Long>, ChatroomRepositoryCustom {

    Optional<Chatroom> findByUuid(String uuid);

}
