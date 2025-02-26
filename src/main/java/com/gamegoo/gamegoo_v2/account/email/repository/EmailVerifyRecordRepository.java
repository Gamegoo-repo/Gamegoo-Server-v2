package com.gamegoo.gamegoo_v2.account.email.repository;

import com.gamegoo.gamegoo_v2.account.email.domain.EmailVerifyRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface EmailVerifyRecordRepository extends JpaRepository<EmailVerifyRecord, Long> {

    @Query("SELECT e FROM EmailVerifyRecord e WHERE e.email = :email AND e.createdAt >= :timeLimit")
    List<EmailVerifyRecord> findRecentRecordsByEmail(@Param("email") String email,
                                                     @Param("timeLimit") LocalDateTime timeLimit);

    Optional<EmailVerifyRecord> findTop1ByEmailOrderByCreatedAtDesc(String email);

}
