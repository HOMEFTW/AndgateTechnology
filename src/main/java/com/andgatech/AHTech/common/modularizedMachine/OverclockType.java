package com.andgatech.AHTech.common.modularizedMachine;

public enum OverclockType {

    NONE(1, 1),
    NormalOverclock(2, 4),
    LowSpeedPerfectOverclock(2, 2),
    PerfectOverclock(4, 4),
    SingularityPerfectOverclock(8, 4),
    EOHStupidOverclock(2, 8);

    public final int timeReduction;
    public final int powerIncrease;
    public final boolean perfectOverclock;

    OverclockType(int timeReduction, int powerIncrease) {
        this.timeReduction = timeReduction;
        this.powerIncrease = powerIncrease;
        this.perfectOverclock = timeReduction >= powerIncrease;
    }

    public boolean isPerfectOverclock() {
        return perfectOverclock;
    }

    public static OverclockType checkOverclockType(int timeReduction, int powerIncrease) {
        for (OverclockType t : values()) {
            if (t.timeReduction == timeReduction && t.powerIncrease == powerIncrease) {
                return t;
            }
        }
        return NormalOverclock;
    }

    public int getID() {
        return ordinal();
    }

    public static OverclockType getFromID(int id) {
        OverclockType[] values = values();
        return id >= 0 && id < values.length ? values[id] : NormalOverclock;
    }
}
