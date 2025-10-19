package edu.hitsz.dao;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

public class Player implements Serializable {
    private int rank;
    private String name;
    private int score;
    private LocalDateTime recordTime;

    public Player(String name, int score, LocalDateTime recordTime) {
        this.name = name;
        this.score = score;
        this.recordTime = recordTime;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public LocalDateTime getRecordTime() {
        return recordTime;
    }

    public void setRecordTime(LocalDateTime recordTime) {
        this.recordTime = recordTime;
    }
}
