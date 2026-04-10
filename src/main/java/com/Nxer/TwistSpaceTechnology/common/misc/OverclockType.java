package com.Nxer.TwistSpaceTechnology.common.misc;

/**
 * Stub for TST OverclockType enum.
 */
public enum OverclockType {

    NONE(1, 1),
    NormalOverclock(2, 4),
    LowSpeedPerfectOverclock(2, 2),
    PerfectOverclock(4, 4),
    SingularityPerfectOverclock(8, 4),
    EOHStupidOverclock(2, 8);

    public final int timeReduction;
    public final int powerIncrease;

    OverclockType(int timeReduction, int powerIncrease) {
        this.timeReduction = timeReduction;
        this.powerIncrease = powerIncrease;
    }
}
