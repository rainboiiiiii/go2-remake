package com.go2super.listener;

import com.go2super.database.entity.Planet;
import com.go2super.database.entity.User;
import com.go2super.database.entity.sub.HumaroidPlanet;
import com.go2super.database.entity.sub.ResourcePlanet;
import com.go2super.database.entity.sub.UserPlanet;
import com.go2super.obj.game.MapArea;
import com.go2super.obj.utility.*;
import com.go2super.packet.PacketListener;
import com.go2super.packet.PacketProcessor;
import com.go2super.packet.map.*;
import com.go2super.service.GalaxyService;
import com.go2super.service.LoginService;
import com.go2super.service.exception.BadGuidException;

import java.util.ArrayList;
import java.util.List;

public class MapListener implements PacketListener {

    @PacketProcessor
    public void onMapBlock(RequestMapBlockPacket packet) throws BadGuidException {

        LoginService.validate(packet, packet.getGuid());

        ResponseMapBlockFightPacket responseMapBlockFightPacket = new ResponseMapBlockFightPacket();

        responseMapBlockFightPacket.setBlockId(0);
        responseMapBlockFightPacket.setGalaxyMapId((short) 0);
        responseMapBlockFightPacket.setDataLen((short) 0);

        packet.getSmartServer().send(responseMapBlockFightPacket);

    }

    @PacketProcessor
    public void onMapArea(RequestMapAreaPacket packet) {

        if(packet.getRegionId().getArray().length > 16)
            return;

        for(int regionId : packet.getRegionId().getArray()) {

            GalaxyRegion region = new GalaxyRegion(regionId);
            List<Planet> planets = GalaxyService.getInstance().getPlanetRepository().getPlanets(region);
            List<Integer> excludeDummy = new ArrayList<>();

            for(Planet planet : planets) {
                if(planet instanceof UserPlanet) {
                    excludeDummy.add(planet.getPosition().galaxyId());
                }
            }

            ResponseMapAreaPacket responseMapAreaPacket = new ResponseMapAreaPacket();
            List<MapArea> dummies = GalaxyService.getInstance().getDummyValues(regionId, true, excludeDummy);

            if(!planets.isEmpty())
                for(Planet planet : planets)
                    if(planet instanceof UserPlanet) {

                        User user = ((UserPlanet) planet).getUser();
                        user.update();

                        dummies.add(new MapArea("Unknow",
                                user.getUsername(),
                                user.getPlanet().getPosition().galaxyId(),
                                GalaxyService.getInstance().getUserPlanet(user).getPosition().galaxyId(),
                                0,
                                user.getStarFace(),
                                1,
                                21,
                                5,
                                user.getStarType(),
                                0,
                                -1,
                                user.getSpaceStationLevel()));
                    } else if(planet instanceof HumaroidPlanet) {
                        HumaroidPlanet humaroid = (HumaroidPlanet) planet;
                        dummies.add(new MapArea("",
                                "Humaroid",
                                humaroid.getUserId(),
                                humaroid.getPosition().galaxyId(),
                                0,
                                -1,
                                1,
                                0,
                                0,
                                2,
                                0,
                                -1,
                                1));
                    } else if(planet instanceof ResourcePlanet) {
                        ResourcePlanet rbp = (ResourcePlanet) planet;
                        dummies.add(new MapArea("",
                                "Resource Bonus Planet",
                                rbp.getUserId(),
                                rbp.getPosition().galaxyId(),
                                0,
                                -1,
                                1,
                                0,
                                0,
                                3,
                                0,
                                -1,
                                1));
                    }


            responseMapAreaPacket.setGalaxyMapId((char) 0);
            responseMapAreaPacket.setRegionId(UnsignedShort.of(regionId));
            responseMapAreaPacket.setDataLen(UnsignedChar.of(dummies.size()));

            responseMapAreaPacket.setMapAreaList(dummies);

            packet.getSmartServer().send(responseMapAreaPacket);

        }

    }

}
