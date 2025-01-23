package com.gamegoo.gamegoo_v2.content.board.domain;

import com.gamegoo.gamegoo_v2.account.member.domain.Member;
import com.gamegoo.gamegoo_v2.account.member.domain.Mike;
import com.gamegoo.gamegoo_v2.account.member.domain.Position;
import com.gamegoo.gamegoo_v2.core.common.BaseDateTimeEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
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

    @Column(nullable = false)
    private int mode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Position mainP;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Position subP;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Position wantP;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Mike mike;

    @Column(length = 5000)
    private String content;

    @Column(nullable = false)
    private int boardProfileImage;

    @Column(nullable = false)
    private boolean deleted = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @OneToMany(mappedBy = "board", cascade = CascadeType.PERSIST, orphanRemoval = true)
    private List<BoardGameStyle> boardGameStyles = new ArrayList<>();


    public static Board create(Member member, int mode, Position mainP, Position subP,
                               Position wantP,
                               Mike mike, String content, int boardProfileImage) {
        return Board.builder()
                .member(member)
                .mode(mode)
                .mainP(mainP)
                .subP(subP)
                .wantP(wantP)
                .mike(mike)
                .content(content)
                .boardProfileImage(boardProfileImage)
                .build();
    }

    @Builder
    private Board(int mode, Position mainP, Position subP, Position wantP, Mike mike,
                  String content,
                  int boardProfileImage, boolean deleted, Member member) {
        this.mode = mode;
        this.mainP = mainP;
        this.subP = subP;
        this.wantP = wantP;
        this.mike = mike;
        this.content = content;
        this.boardProfileImage = boardProfileImage;
        this.deleted = deleted;
        this.member = member;
    }

    public void addBoardGameStyle(BoardGameStyle boardGameStyle) {
        boardGameStyles.add(boardGameStyle);
        boardGameStyle.setBoard(this);
    }

    public void removeBoardGameStyle(BoardGameStyle boardGameStyle) {
        boardGameStyles.remove(boardGameStyle);
        boardGameStyle.removeBoard();
    }

    public void updateBoard(int gameMode, Position mainP, Position subP, Position wantP, Mike mike,
                            String content, int boardProfileImage) {
        if (gameMode != 0) {
            this.mode = gameMode;
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


}
