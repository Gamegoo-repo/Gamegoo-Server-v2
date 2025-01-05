package com.gamegoo.gamegoo_v2.account.member.domain;

import com.gamegoo.gamegoo_v2.core.common.BaseDateTimeEntity;
import com.gamegoo.gamegoo_v2.notification.domain.Notification;
import com.gamegoo.gamegoo_v2.social.friend.domain.Friend;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Entity
@Getter
@Table(name = "Member")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends BaseDateTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long id;

    @Column(nullable = false, length = 100)
    private String email;

    @Column(nullable = false, length = 500)
    private String password;

    @Column(nullable = false)
    private int profileImage;

    @Column(nullable = false)
    private int mannerLevel = 1;

    private Integer mannerScore;

    @Column(nullable = false)
    private boolean blind = false;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "VARCHAR(20)")
    private LoginType loginType;

    @Column(nullable = false, length = 100)
    private String gameName;

    @Column(nullable = false, length = 100)
    private String tag;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Tier tier;

    @Column(nullable = false)
    private int gameRank;

    @Column(nullable = false)
    private double winRate;

    @Column(nullable = false)
    private int mainPosition = 0;

    @Column(nullable = false)
    private int subPosition = 0;

    @Column(nullable = false)
    private int wantPosition = 0;

    @Column(nullable = false)
    private boolean mike = false;

    @Column(nullable = false)
    private int gameCount;

    @Column(nullable = false)
    private boolean isAgree;

    @OneToMany(mappedBy = "fromMember", cascade = CascadeType.ALL)
    private List<Friend> friendList = new ArrayList<>();

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
    private List<Notification> notificationList = new ArrayList<>();

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
    private List<MemberChampion> memberChampionList = new ArrayList<>();

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
    private List<MemberGameStyle> memberGameStyleList = new ArrayList<>();

    // 회원가입용 create
    public static Member create(String email, String password, LoginType loginType, String gameName, String tag,
                                Tier tier, int gameRank, double winRate, int gameCount, boolean isAgree) {
        int randomProfileImage = ThreadLocalRandom.current().nextInt(1, 9);

        return Member.builder()
                .email(email)
                .password(password)
                .profileImage(randomProfileImage)
                .loginType(loginType)
                .gameName(gameName)
                .tag(tag)
                .tier(tier)
                .gameCount(gameCount)
                .gameRank(gameRank)
                .winRate(winRate)
                .isAgree(isAgree)
                .build();
    }

    // 회원가입용 Builder
    @Builder
    private Member(String email, String password, int profileImage, LoginType loginType, String gameName,
                   String tag, Tier tier, int gameRank, double winRate, int gameCount, boolean isAgree) {
        this.email = email;
        this.password = password;
        this.profileImage = profileImage;
        this.loginType = loginType;
        this.gameName = gameName;
        this.tag = tag;
        this.tier = tier;
        this.gameRank = gameRank;
        this.winRate = winRate;
        this.gameCount = gameCount;
        this.isAgree = isAgree;
    }

    public void updateBlind(boolean blind) {
        this.blind = blind;
    }

    public void updateProfileImage(int profileImage) {
        this.profileImage = profileImage;
    }

    public void updateMike(boolean mike) {
        this.mike = mike;
    }

    public void updatePosition(int mainPosition, int subPosition, int wantPosition) {
        this.mainPosition = mainPosition;
        this.subPosition = subPosition;
        this.wantPosition = wantPosition;
    }

    public void updatePassword(String password) {
        this.password = password;
    }

    public Integer updateMannerScore(int mannerScore) {
        this.mannerScore = mannerScore;
        return this.mannerScore;
    }

    public int updateMannerLevel(int mannerLevel) {
        this.mannerLevel = mannerLevel;
        return this.mannerLevel;
    }

}
