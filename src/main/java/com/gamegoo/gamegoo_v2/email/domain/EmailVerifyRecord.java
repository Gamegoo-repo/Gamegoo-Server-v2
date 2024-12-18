package com.gamegoo.gamegoo_v2.email.domain;

import com.gamegoo.gamegoo_v2.common.BaseDateTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EmailVerifyRecord extends BaseDateTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "record_id")
    private Long id;

    @Column(nullable = false, length = 100)
    private String email;

    @Column(nullable = false, length = 10)
    private String code;

    public static EmailVerifyRecord create(String email, String code) {
        return EmailVerifyRecord.builder()
                .email(email)
                .code(code)
                .build();
    }

    @Builder
    private EmailVerifyRecord(String email, String code) {
        this.email = email;
        this.code = code;
    }

    @Builder
    public EmailVerifyRecord(String email, String code, LocalDateTime updatedAt) {
        this.email = email;
        this.code = code;
        this.updateUpdatedAt(updatedAt);
    }


}
