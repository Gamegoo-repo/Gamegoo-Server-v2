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
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Entity
@Getter
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

    private Double mannerRank;

    @Column(nullable = false)
    private boolean blind = false;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "VARCHAR(50)")
    private LoginType loginType;

    @Column(nullable = false, length = 100)
    private String gameName;

    @Column(nullable = false, length = 100)
    private String tag;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "VARCHAR(50)")
    private Tier soloTier = Tier.UNRANKED;

    @Column(nullable = false)
    private int soloRank = 0;

    @Column(nullable = false)
    private double soloWinRate = 0.0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Tier freeTier = Tier.UNRANKED;

    @Column(nullable = false)
    private int freeRank = 0;

    @Column(nullable = false)
    private double freeWinRate = 0.0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "VARCHAR(20)")
    private Position mainPosition = Position.ANY;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Position subPosition = Position.ANY;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "VARCHAR(20)")
    private Position wantPosition = Position.ANY;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "VARCHAR(50)")
    private Mike mike = Mike.UNAVAILABLE;

    @Column(nullable = false)
    private int soloGameCount = 0;

    @Column(nullable = false)
    private int freeGameCount = 0;

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

    public static Member create(String email, String password, LoginType loginType, String gameName, String tag,
                                Tier soloTier, int soloRank, double soloWinRate, Tier freeTier, int freeRank,
                                double freeWinRate, int soloGameCount, int freeGameCount, boolean isAgree) {
        int randomProfileImage = ThreadLocalRandom.current().nextInt(1, 9);

        return Member.builder()
                .email(email)
                .password(password)
                .profileImage(randomProfileImage)
                .loginType(loginType)
                .gameName(gameName)
                .tag(tag)
                .soloGameCount(soloGameCount)
                .freeGameCount(freeGameCount)
                .soloTier(soloTier)
                .soloRank(soloRank)
                .soloWinRate(soloWinRate)
                .freeTier(freeTier)
                .freeRank(freeRank)
                .freeWinRate(freeWinRate)
                .isAgree(isAgree)
                .build();
    }

    @Builder
    private Member(String email, String password, int profileImage, LoginType loginType, String gameName,
                   String tag, Tier soloTier, int soloRank, double soloWinRate, Tier freeTier, int freeRank,
                   double freeWinRate, int soloGameCount, int freeGameCount, boolean isAgree) {
        this.email = email;
        this.password = password;
        this.profileImage = profileImage;
        this.loginType = loginType;
        this.gameName = gameName;
        this.tag = tag;
        this.soloTier = soloTier;
        this.soloRank = soloRank;
        this.soloWinRate = soloWinRate;
        this.soloGameCount = soloGameCount;
        this.freeTier = freeTier;
        this.freeRank = freeRank;
        this.freeWinRate = freeWinRate;
        this.freeGameCount = freeGameCount;
        this.isAgree = isAgree;
    }

    public void updateBlind(boolean blind) {
        this.blind = blind;
    }

    public void updateProfileImage(int profileImage) {
        this.profileImage = profileImage;
    }

    public void updateMike(Mike mike) {
        this.mike = mike;
    }

    public void updatePosition(Position mainPosition, Position subPosition, Position wantPosition) {
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

    public Double updateMannerRank(Double mannerRank) {
        this.mannerRank = mannerRank;
        return this.mannerRank;
    }

    public void updateMemberByMatchingRecord(Mike mike, Position mainP, Position subP, Position wantP) {
        this.mike = mike;
        this.mainPosition = mainP;
        this.subPosition = subP;
        this.wantPosition = wantP;
    }

}
