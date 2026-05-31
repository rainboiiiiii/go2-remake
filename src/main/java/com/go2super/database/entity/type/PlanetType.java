package com.go2super.database.entity.type;

import lombok.Getter;

public enum PlanetType {
    USER_PLANET(0),
    HUMAROID_PLANET(2),
    RESOURCES_PLANET(3);

    @Getter
    private int code;

    PlanetType(int code) {
        this.code = code;
    }

}
