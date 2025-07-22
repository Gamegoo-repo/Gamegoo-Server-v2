package com.gamegoo.gamegoo_v2.account.member.domain;

import com.gamegoo.gamegoo_v2.account.auth.domain.Role;
import com.gamegoo.gamegoo_v2.core.common.BaseDateTimeEntity;
import com.gamegoo.gamegoo_v2.notification.domain.Notification;
import com.gamegoo.gamegoo_v2.social.friend.domain.Friend;
import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicUpdate;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Entity
@DynamicUpdate
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends BaseDateTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long id;

    @Column(length = 100)
    private String email;

    @Column(length = 200)
    private String puuid;

    @Column(length = 500)
    private String password;

    @Column(nullable = false)
    private int profileImage;

    @Column(nullable = false)
    private int mannerLevel = 1;

    private Integer mannerScore;

    private Double mannerRank;

    @Column(nullable = false)
    private Boolean blind = false;

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
    @Column(nullable = false, columnDefinition = "VARCHAR(50)")
    private Tier freeTier = Tier.UNRANKED;

    @Column(nullable = false)
    private int freeRank = 0;

    @Column(nullable = false)
    private double freeWinRate = 0.0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "VARCHAR(20)")
    private Position mainP = Position.ANY;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "VARCHAR(50)")
    private Position subP = Position.ANY;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "member_want_positions", joinColumns = @JoinColumn(name = "member_id"))
    @Column(name = "want_position", nullable = false)
    @Enumerated(EnumType.STRING)
    private List<Position> wantP = new ArrayList<>();

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

    @OneToOne(mappedBy = "member", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private MemberRecentStats memberRecentStats;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "VARCHAR(20)")
    private BanType banType = BanType.NONE;

    private java.time.LocalDateTime banExpireAt;

    @Column(name = "champion_stats_refreshed_at")
    private java.time.LocalDateTime championStatsRefreshedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "VARCHAR(20)")
    private Role role = Role.MEMBER;

    public void setMemberRecentStats(MemberRecentStats stats) {
        this.memberRecentStats = stats;
        if (stats.getMember() != this) {
            stats.setMember(this);
        }
    }

    // puuid 전용
    public static Member createForGeneral(String email, String password, LoginType loginType, String gameName,
                                          String tag,
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

    // RSO 전용
    public static Member createForRiot(String puuid, LoginType loginType, String gameName,
                                       String tag, Tier soloTier, int soloRank, double soloWinRate, Tier freeTier,
                                       int freeRank,
                                       double freeWinRate, int soloGameCount, int freeGameCount, boolean isAgree) {
        int randomProfileImage = ThreadLocalRandom.current().nextInt(1, 9);

        return Member.builder()
                .profileImage(randomProfileImage)
                .puuid(puuid)
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

    // 임시 멤버 전용
    public static Member createForTmp(String gameName, String tag, Tier soloTier, int soloRank, double soloWinRate, 
                                      Tier freeTier, int freeRank, double freeWinRate, int soloGameCount, int freeGameCount) {
        // 임시 멤버 랜덤 프로필 사진 설정
        int randomProfileImage = ThreadLocalRandom.current().nextInt(1, 9);

        return Member.builder()
                .profileImage(randomProfileImage)
                .loginType(LoginType.GENERAL)
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
                .isAgree(true)
                .build();
    }

    @Builder
    private Member(String email, String puuid, String password, int profileImage, LoginType loginType, String gameName,
                   String tag, Tier soloTier, int soloRank, double soloWinRate, Tier freeTier, int freeRank,
                   double freeWinRate, int soloGameCount, int freeGameCount, boolean isAgree) {
        this.email = email;
        this.password = password;
        this.puuid = puuid;
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

    public void updatePosition(Position mainPosition, Position subPosition, List<Position> wantPositions) {
        this.mainP = mainPosition;
        this.subP = subPosition;
        this.wantP = wantPositions;
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

    public void updateMemberByMatchingRecord(Mike mike, Position mainP, Position subP, List<Position> wantPositions) {
        this.mike = mike;
        this.mainP = mainP;
        this.subP = subP;
        this.wantP = wantPositions;
    }

    public void deactiveMember() {
        this.blind = true;
    }

    public void applyBan(BanType banType, java.time.LocalDateTime banExpireAt) {
        this.banType = banType;
        this.banExpireAt = banExpireAt;
    }

    public void releaseBan() {
        this.banType = BanType.NONE;
        this.banExpireAt = null;
    }

    public void updateRole(Role role) {
        this.role = role;
    }

    public void updateChampionStatsRefreshedAt() {
        this.championStatsRefreshedAt = java.time.LocalDateTime.now();
    }

    public boolean isBanned() {
        if (banType == BanType.NONE) {
            return false;
        }
        if (banType == BanType.PERMANENT) {
            return true;
        }
        if (banExpireAt == null) {
            return false;
        }
        return java.time.LocalDateTime.now().isBefore(banExpireAt);
    }

    public boolean canWritePost() {
        return !isBanned();
    }

    public boolean canChat() {
        return !isBanned();
    }

    public boolean canMatch() {
        return !isBanned();
    }

}
