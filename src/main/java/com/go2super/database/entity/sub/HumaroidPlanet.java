package com.go2super.database.entity.sub;

import com.go2super.database.entity.Planet;
import com.go2super.database.entity.type.PlanetType;
import com.go2super.obj.utility.GalaxyTile;
import com.go2super.socket.util.MathUtil;
import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Data
public class HumaroidPlanet extends Planet {

    private int currentLevel;

    private boolean fight;
    private boolean defeated;

    private Date protectionTime;

    public HumaroidPlanet() {

    }

    public HumaroidPlanet(GalaxyTile galaxyTile, long userId) {

        this.currentLevel = MathUtil.random(0, 15);
        this.fight = false;
        this.protectionTime = new Date();

        this.setUserId(userId);
        this.setType(PlanetType.HUMAROID_PLANET);
        this.setPosition(galaxyTile);

    }

}
