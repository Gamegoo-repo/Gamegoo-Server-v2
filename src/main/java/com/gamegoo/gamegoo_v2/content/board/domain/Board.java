package com.gamegoo.gamegoo_v2.content.board.domain;

import com.gamegoo.gamegoo_v2.account.member.domain.Member;
import com.gamegoo.gamegoo_v2.account.member.domain.Mike;
import com.gamegoo.gamegoo_v2.account.member.domain.Position;
import com.gamegoo.gamegoo_v2.core.common.BaseDateTimeEntity;
import com.gamegoo.gamegoo_v2.matching.domain.GameMode;
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
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Formula;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Board extends BaseDateTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "board_id")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "VARCHAR(20)")
    private GameMode gameMode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "VARCHAR(20)")
    private Position mainP;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "VARCHAR(20)")
    private Position subP;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "board_want_positions", joinColumns = @JoinColumn(name = "board_id"))
    @Column(name = "wantP", nullable = false)
    @Enumerated(EnumType.STRING)
    private List<Position> wantP = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "VARCHAR(20)")
    private Mike mike;

    @Column(length = 5000)
    private String content;

    @Column(nullable = false)
    private int boardProfileImage;

    @Column(nullable = false)
    private boolean deleted = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = true)
    private Member member;

    @Column(length = 50)
    private String gameName;

    @Column(length = 10)
    private String tag;

    @Column(nullable = false)
    private boolean isGuest = false;

    @Column(length = 16)
    private String guestPassword;

    @OneToMany(mappedBy = "board", cascade = CascadeType.PERSIST, orphanRemoval = true)
    private List<BoardGameStyle> boardGameStyles = new ArrayList<>();

    private LocalDateTime bumpTime;

    @Formula("GREATEST(COALESCE(bump_time, created_at), created_at)")
    private LocalDateTime activityTime;


    public static Board create(Member member, GameMode gameMode, Position mainP, Position subP,
                               List<Position> wantP,
                               Mike mike, String content, int boardProfileImage) {
        return Board.builder()
                .member(member)
                .isGuest(false)
                .gameMode(gameMode)
                .mainP(mainP)
                .subP(subP)
                .wantP(wantP)
                .mike(mike)
                .content(content)
                .boardProfileImage(boardProfileImage)
                .build();
    }

    public static Board createForGuest(String gameName, String tag, GameMode gameMode, Position mainP, Position subP,
                                       List<Position> wantP, Mike mike, String content, int boardProfileImage, String guestPassword) {
        return Board.builder()
                .gameName(gameName)
                .tag(tag)
                .isGuest(true)
                .gameMode(gameMode)
                .mainP(mainP)
                .subP(subP)
                .wantP(wantP)
                .mike(mike)
                .content(content)
                .boardProfileImage(boardProfileImage)
                .guestPassword(guestPassword)
                .build();
    }

    @Builder
    private Board(GameMode gameMode, Position mainP, Position subP, List<Position> wantP, Mike mike,
                  String content, int boardProfileImage, boolean deleted, Member member,
                  String gameName, String tag, boolean isGuest, String guestPassword) {
        this.gameMode = gameMode;
        this.mainP = mainP;
        this.subP = subP;
        this.wantP = wantP;
        this.mike = mike;
        this.content = content;
        this.boardProfileImage = boardProfileImage;
        this.deleted = deleted;
        this.member = member;
        this.gameName = gameName;
        this.tag = tag;
        this.isGuest = isGuest;
        this.guestPassword = guestPassword;
    }

    public void addBoardGameStyle(BoardGameStyle boardGameStyle) {
        boardGameStyles.add(boardGameStyle);
        boardGameStyle.setBoard(this);
    }

    public void removeBoardGameStyle(BoardGameStyle boardGameStyle) {
        boardGameStyles.remove(boardGameStyle);
        boardGameStyle.removeBoard();
    }

    public void updateBoard(GameMode gameMode, Position mainP, Position subP, List<Position> wantP, Mike mike,
                            String content, int boardProfileImage) {
        if (gameMode != null) {
            this.gameMode = gameMode;
        }
        if (mainP != null) {
            this.mainP = mainP;
        }
        if (subP != null) {
            this.subP = subP;
        }
        if (wantP != null) {
            this.wantP = wantP;
        }
        if (mike != null) {
            this.mike = mike;
        }

        if (content == null) {
            this.content = "";
        } else {
            this.content = content;
        }

        this.boardProfileImage = boardProfileImage;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public void bump(LocalDateTime bumpTime) {
        this.bumpTime = bumpTime;
    }

    public LocalDateTime getActivityTime() {
        return activityTime;
    }

    public boolean verifyGuestPassword(String password) {
        return this.guestPassword != null && this.guestPassword.equals(password);
    }


}
