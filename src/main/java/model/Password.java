package model;

import java.io.Serializable;

public class Password implements Serializable {
    private String password = null;
    private boolean initial = true;
    private String range =null;
    private long currentSeed = 0;

    public void setCurrentSeed(long currentSeed) {
        this.currentSeed = currentSeed;
    }

    public long getCurrentSeed() {
        return currentSeed;
    }

    public String getRange() {
        return range;
    }

    public void setRange(String range) {
        this.range = range;
    }

    public boolean isInitial() {
        return initial;
    }

    public void setInitial(boolean initial) {
        this.initial = initial;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
