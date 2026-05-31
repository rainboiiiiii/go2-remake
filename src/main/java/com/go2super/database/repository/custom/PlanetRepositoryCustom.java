package com.go2super.database.repository.custom;

import com.go2super.database.entity.Planet;
import com.go2super.database.entity.User;
import com.go2super.database.entity.sub.UserPlanet;
import com.go2super.obj.utility.GalaxyRegion;
import com.go2super.obj.utility.GalaxyTile;

import java.util.List;

public interface PlanetRepositoryCustom {

    UserPlanet getUserPlanet(User user);

    UserPlanet getUserPlanet(long userId);

    List<GalaxyTile> getTakedPositions();

    List<Planet> getPlanets(GalaxyRegion galaxyRegion);

    List<UserPlanet> getUserPlanets(GalaxyRegion galaxyRegion);

}
