package com.gamegoo.gamegoo_v2.chat.domain;

import com.gamegoo.gamegoo_v2.core.common.BaseDateTimeEntity;
import com.gamegoo.gamegoo_v2.account.member.domain.Member;
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
public class MemberChatroom extends BaseDateTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_chatroom_id")
    private Long id;

    private LocalDateTime lastViewDate;

    private LocalDateTime lastJoinDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chatroom_id", nullable = false)
    private Chatroom chatroom;

    public static MemberChatroom create(Member member, Chatroom chatroom, LocalDateTime lastJoinDate) {
        return MemberChatroom.builder()
                .lastViewDate(null)
                .lastJoinDate(lastJoinDate)
                .member(member)
                .chatroom(chatroom)
                .build();
    }

    @Builder
    private MemberChatroom(LocalDateTime lastViewDate, LocalDateTime lastJoinDate, Member member, Chatroom chatroom) {
        this.lastViewDate = lastViewDate;
        this.lastJoinDate = lastJoinDate;
        this.member = member;
        this.chatroom = chatroom;
    }

    public boolean exited() {
        return this.lastJoinDate == null;
    }

    public void updateLastViewDate(LocalDateTime lastViewDate) {
        this.lastViewDate = lastViewDate;
    }

    public void updateLastJoinDate(LocalDateTime lastJoinDate) {
        this.lastJoinDate = lastJoinDate;
    }

}
