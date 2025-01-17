package com.gamegoo.gamegoo_v2.content.board.domain;

import com.gamegoo.gamegoo_v2.core.common.BaseDateTimeEntity;
import com.gamegoo.gamegoo_v2.game.domain.GameStyle;
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
public class BoardGameStyle extends BaseDateTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "board_game_style_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gamestyle_id", nullable = false)
    private GameStyle gameStyle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id", nullable = false)
    private Board board;

    @Builder
    private BoardGameStyle(GameStyle gameStyle, Board board) {
        this.gameStyle = gameStyle;
        this.board = board;
    }

    public static BoardGameStyle create(GameStyle gameStyle, Board board) {
        return BoardGameStyle.builder()
                .gameStyle(gameStyle)
                .board(board)
                .build();
    }

    public void setBoard(Board board) {
        if (this.board != null) {
            this.board.getBoardGameStyles().remove(this);
        }
        this.board = board;
        if (board != null && !board.getBoardGameStyles().contains(this)) {
            board.getBoardGameStyles().add(this);
        }
    }

    public void removeBoard() {
        if (this.board != null) {
            this.board.getBoardGameStyles().remove(this);
            this.board = null;
        }
    }

}
