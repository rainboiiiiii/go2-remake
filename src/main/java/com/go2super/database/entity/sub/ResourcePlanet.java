package com.go2super.database.entity.sub;

import com.go2super.database.entity.Planet;
import com.go2super.database.entity.type.PlanetType;
import com.go2super.obj.utility.GalaxyTile;
import com.go2super.socket.util.MathUtil;
import lombok.Data;

import java.util.Date;

@Data
public class ResourcePlanet extends Planet {

    private int currentCorp;
    private int currentLevel;

    private boolean fight;

    private Date protectionTime;

    public ResourcePlanet() {

    }

    public ResourcePlanet(GalaxyTile galaxyTile, long userId) {

        this.currentCorp = -1;
        this.currentLevel = 0;
        this.fight = false;
        this.protectionTime = new Date();

        this.setUserId(userId);
        this.setType(PlanetType.RESOURCES_PLANET);
        this.setPosition(galaxyTile);

    }

}
