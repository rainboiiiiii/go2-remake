package com.go2super.database.entity.type;

import lombok.Getter;

public enum MatchType {
    INSTANCE_MATCH(0, true),
    ARENA_MATCH(1, true),
    RAIDS_MATCH(2, true),
    LEAGUE_MATCH(3, true),
    CHAMPION_MATCH(4, true),
    IGL_MATCH(5, true),
    PVP_MATCH(6, false),
    RBP_MATCH(7, false),
    HUMAROID_MATCH(8, false),
    PIRATES_MATCH(9, false)

    ;

    @Getter
    private int code;

    @Getter
    private boolean virtual;

    MatchType(int code, boolean virtual) {
        this.code = code;
        this.virtual = virtual;
    }

}
