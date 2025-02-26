package com.gamegoo.gamegoo_v2.social.friend.domain;

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

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Friend extends BaseDateTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "friend_id")
    private Long id;

    @Column(nullable = false)
    private boolean liked = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_member_id", nullable = false)
    private Member fromMember;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_member_id", nullable = false)
    private Member toMember;

    public static Friend create(Member fromMember, Member toMember) {
        Friend friend = Friend.builder()
                .liked(false)
                .toMember(toMember)
                .build();
        friend.setFromMember(fromMember); // 양방향 관계 설정
        return friend;
    }

    @Builder
    private Friend(boolean liked, Member fromMember, Member toMember) {
        this.liked = liked;
        this.fromMember = fromMember;
        this.toMember = toMember;
    }

    public void setFromMember(Member member) {
        if (this.fromMember != null) {
            this.fromMember.getFriendList().remove(this);
        }
        this.fromMember = member;
        member.getFriendList().add(this);
    }

    public void reverseLiked() {
        this.liked = !this.liked;
    }

}
