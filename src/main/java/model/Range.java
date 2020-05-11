package model;

import java.io.Serializable;

public class Range implements Serializable {
    private long minSeed;
    private long maxSeed;
    private boolean correct = false;
    private long currentSeed=0;
    private boolean initial = true;
    private String solvedBy = null;

    public long getMinSeed() {
        return minSeed;
    }

    public void setMinSeed(long minSeed) {
        this.minSeed = minSeed;
    }

    public long getMaxSeed() {
        return maxSeed;
    }

    public void setMaxSeed(long maxSeed) {
        this.maxSeed = maxSeed;
    }

    public boolean isCorrect() {
        return correct;
    }

    public void setCorrect(boolean correct) {
        this.correct = correct;
    }

    public long getCurrentSeed() {
        return currentSeed;
    }

    public void setCurrentSeed(long currentSeed) {
        this.currentSeed = currentSeed;
    }

    public boolean isInitial() {
        return initial;
    }

    public void setInitial(boolean initial) {
        this.initial = initial;
    }

    public String getSolvedBy() {
        return solvedBy;
    }

    public void setSolvedBy(String solvedBy) {
        this.solvedBy = solvedBy;
    }
}
