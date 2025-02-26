package com.gamegoo.gamegoo_v2.chat.domain;

import com.gamegoo.gamegoo_v2.account.member.domain.Member;
import com.gamegoo.gamegoo_v2.content.board.domain.Board;
import com.gamegoo.gamegoo_v2.core.common.BaseDateTimeEntity;
import com.gamegoo.gamegoo_v2.utils.TimestampUtil;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Chat extends BaseDateTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "chat_id")
    private Long id;

    @Column(nullable = false, length = 1000)
    private String contents;

    @Column(nullable = false)
    private long timestamp;

    private Integer systemType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chatroom_id", nullable = false)
    private Chatroom chatroom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_member_id", nullable = false)
    private Member fromMember;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_member_id")
    private Member toMember;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_board_id")
    private Board sourceBoard;

    public static Chat create(String contents, Integer systemType, Chatroom chatroom, Member fromMember,
                              Member toMember, Board sourceBoard) {
        return Chat.builder()
                .contents(contents)
                .systemType(systemType)
                .chatroom(chatroom)
                .fromMember(fromMember)
                .toMember(toMember)
                .sourceBoard(sourceBoard)
                .timestamp(TimestampUtil.getNowUtcTimeStamp())
                .build();
    }

    @Builder
    private Chat(String contents, long timestamp, Integer systemType, Chatroom chatroom, Member fromMember,
                 Member toMember, Board sourceBoard) {
        this.contents = contents;
        this.timestamp = timestamp;
        this.systemType = systemType;
        this.chatroom = chatroom;
        this.fromMember = fromMember;
        this.toMember = toMember;
        this.sourceBoard = sourceBoard;
    }

    /**
     * 테스트 전용 createdAt 설정 메소드
     *
     * @param createdAt
     * @return
     */
    public Chat withCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
        return this;
    }

}
