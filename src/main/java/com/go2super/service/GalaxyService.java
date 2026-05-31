package com.go2super.service;

import com.go2super.database.entity.Planet;
import com.go2super.database.entity.User;
import com.go2super.database.entity.sub.UserPlanet;
import com.go2super.database.repository.PlanetRepository;
import com.go2super.obj.game.MapArea;
import com.go2super.obj.utility.GalaxyTile;
import com.go2super.obj.utility.GalaxyZone;
import com.go2super.obj.utility.GameCell;
import com.go2super.resources.ResourceManager;
import com.go2super.resources.data.GalaxyMapData;
import com.go2super.resources.json.GalaxyMapJson;
import com.go2super.socket.util.MathUtil;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Getter
@Service
public class GalaxyService {

    private static GalaxyService instance;

    private int currentZones = 210;

    private List<GalaxyTile> possiblePlanetPositions;

    private List<GalaxyTile> possibleHumaroidPositions;
    private List<GalaxyTile> possibleRBPPositions;

    @Autowired
    private PlanetRepository planetRepository;

    public GalaxyService() {

        instance = this;

        this.possiblePlanetPositions = new ArrayList<>();
        this.possibleHumaroidPositions = new ArrayList<>();
        this.possibleRBPPositions = new ArrayList<>();

    }

    public UserPlanet getUserPlanet(long userId) {
        return planetRepository.getUserPlanet(userId);
    }

    public UserPlanet getUserPlanet(User user) {
        return planetRepository.getUserPlanet(user);
    }

    public void calculatePositions() {

        GalaxyMapJson planetsData = ResourceManager.getGalaxyMaps();

        for(int zone = 0; zone < currentZones; zone++) {

            GalaxyZone galaxyZone = new GalaxyZone(zone);

            for(GalaxyMapData mapData : planetsData.getPlayer())
                possiblePlanetPositions.add(mapData.getTile().offset(galaxyZone));

            for(GalaxyMapData mapData : planetsData.getHumaroid())
                possibleHumaroidPositions.add(mapData.getTile().offset(galaxyZone));

            for(GalaxyMapData mapData : planetsData.getCorpBonus())
                possibleRBPPositions.add(mapData.getTile().offset(galaxyZone));

        }

    }

    public Planet getPlanet(GalaxyTile galaxyTile) {
        return planetRepository.findByPosition(galaxyTile);
    }

    public boolean isAvailablePlanetPosition(GameCell cell) {
        Optional<GalaxyTile> result = possiblePlanetPositions.stream().filter(planet -> planet.getX() == cell.getX() && planet.getY() == cell.getY()).findAny();
        return result.isPresent();
    }

    public List<GalaxyTile> getAvailableTiles(int regionId) {
        return possiblePlanetPositions.stream().filter(tile -> tile.getParentRegion().regionId() == regionId).collect(Collectors.toList());
    }

    public List<GalaxyTile> getAvailableHumasTiles(int regionId) {
        return possibleHumaroidPositions.stream().filter(tile -> tile.getParentRegion().regionId() == regionId).collect(Collectors.toList());
    }

    public Optional<GalaxyTile> getAvailablePDRTile(int regionId) {
        return possibleRBPPositions.stream().filter(tile -> tile.getParentRegion().regionId() == regionId).findAny();
    }

    public GalaxyTile randomAvailablePosition() {

        List<GalaxyTile> takedTiles = planetRepository.getTakedPositions();
        List<GalaxyTile> tiles = new ArrayList<>(possiblePlanetPositions);

        tiles.removeAll(takedTiles);
        Collections.shuffle(tiles);

        return tiles.get(0);

    }

    public List<MapArea> getDummyValues(int regionId, boolean some, List<Integer> excludeIds) {

        List<MapArea> mapAreas = new ArrayList<>();
        List<GalaxyTile> planets = getAvailableTiles(regionId);

        for(GalaxyTile planet : planets) {

            if(excludeIds.size() > 0)
                if (excludeIds.contains(planet.galaxyId()))
                    continue;

            mapAreas.add(new MapArea("Dummy_Corp",
                    "Dummy" + UUID.randomUUID().toString().substring(0, 3),
                    MathUtil.random(0, 2000000),
                    planet.galaxyId(),
                    0,
                    -1,
                    1,
                    MathUtil.random(0, 22),
                    MathUtil.random(0, 9),
                    some && MathUtil.random(1, 3) >= 2 ? 1 : 4,
                    0,
                    -1,
                    7));

        }

        return mapAreas;

    }

    public static GalaxyService getInstance() {
        return instance;
    }

}
