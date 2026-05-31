package com.go2super.database.entity.sub;

import com.go2super.database.entity.Planet;
import com.go2super.database.entity.User;
import com.go2super.database.entity.type.PlanetType;
import com.go2super.obj.utility.GalaxyTile;
import com.go2super.service.UserService;
import lombok.Data;

import java.util.Date;

@Data
public class UserPlanet extends Planet {

    private String userObjectId;

    private int starFace;
    private Date untilFlag;

    public UserPlanet() {

    }

    public UserPlanet(String userObjectId, long userId, GalaxyTile galaxyTile) {

        this.userObjectId = userObjectId;
        this.starFace = 0;
        this.untilFlag = new Date();

        this.setUserId(userId);
        this.setType(PlanetType.USER_PLANET);
        this.setPosition(galaxyTile);

    }

    public User getUser() {
        return UserService.getInstance().getUserRepository().findById(userObjectId).get();
    }

}
