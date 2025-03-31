package com.gamegoo.gamegoo_v2.external.riot.domain;

/**
 * 챔피언별 승/패 통계를 저장하는 클래스
 */
public class ChampionStats {

    private final long championId;
    private int wins;
    private int games;
    private int gameTime; // 경기시간(초)
    private int totalMinionsKilled; // cs

    /**
     * 한 경기의 결과로 객체를 생성
     *
     * @param championId 챔피언 ID
     * @param win        해당 경기 승리 여부
     */
    public ChampionStats(long championId, boolean win) {
        this.championId = championId;
        this.games = 1;
        this.wins = win ? 1 : 0;
        this.gameTime = 0;
        this.totalMinionsKilled = 0;
    }

    /**
     * 경기 결과를 추가하여 통계 갱신
     *
     * @param win 승리 여부
     */
    public void addGame(boolean win) {
        this.games++;
        if (win) {
            this.wins++;
        }
    }

    /**
     * 다른 ChampionStats의 통계를 병합
     *
     * @param other 다른 통계 객체
     */
    public void merge(ChampionStats other) {
        this.games += other.games;
        this.wins += other.wins;
        this.gameTime += other.gameTime;
        this.totalMinionsKilled += other.totalMinionsKilled;
    }

    /**
     * 승률 계산 (승리 수 / 경기 수)
     *
     * @return 승률
     */
    public double getWinRate() {
        return games > 0 ? (double) wins / games : 0;
    }

    public long getChampionId() {
        return championId;
    }

    public int getWins() {
        return wins;
    }

    public int getGames() {
        return games;
    }

    public int getGameTime() {
        return gameTime;
    }

    public void setGameTime(int gameTime) {
        this.gameTime = gameTime;
    }

    public int getTotalMinionsKilled() {
        return totalMinionsKilled;
    }

    public void setTotalMinionsKilled(int totalMinionsKilled) {
        this.totalMinionsKilled = totalMinionsKilled;
    }

    /**
     * 분당 CS 계산
     * gameTime이 0이면 안됨
     *
     * @return 분당 CS
     */

    public double getCsPerMinute() {
        if (gameTime > 0) {
            return totalMinionsKilled / (gameTime / 60.0);
        }
        return 0;
    }

}


