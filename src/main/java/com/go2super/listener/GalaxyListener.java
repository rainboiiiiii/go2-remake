package com.go2super.listener;

import com.go2super.database.entity.Fleet;
import com.go2super.database.entity.Planet;
import com.go2super.database.entity.User;
import com.go2super.database.entity.sub.UserPlanet;
import com.go2super.database.entity.type.PlanetType;
import com.go2super.logger.BotLogger;
import com.go2super.obj.game.BuildInfo;
import com.go2super.obj.game.GalaxyFleetInfo;
import com.go2super.obj.model.LoggedGameUser;
import com.go2super.obj.utility.GalaxyTile;
import com.go2super.packet.PacketListener;
import com.go2super.packet.PacketProcessor;
import com.go2super.packet.chat.ResponseGalaxyBroadcastPacket;
import com.go2super.packet.construction.ResponseBuildInfoPacket;
import com.go2super.packet.galaxy.RequestGalaxyPacket;
import com.go2super.packet.ship.ResponseGalaxyShipPacket;
import com.go2super.service.GalaxyService;
import com.go2super.service.LoginService;
import com.go2super.service.PacketService;
import com.go2super.service.UserService;

import javax.swing.plaf.synth.SynthOptionPaneUI;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class GalaxyListener implements PacketListener {

    @PacketProcessor
    public void onGalaxy(RequestGalaxyPacket packet) {

        GalaxyTile tile = new GalaxyTile(packet.getGalaxyId());
        Planet planet = GalaxyService.getInstance().getPlanet(tile);

        if(planet == null)
            return;

        if(planet.getType() != PlanetType.USER_PLANET)
            return; // todo

        UserPlanet userPlanet = (UserPlanet) planet;
        User user = PacketService.getUserRepository().findByUserId(userPlanet.getUserId());

        if(user == null)
            return;

        User requester = PacketService.getUserRepository().findByGuid(packet.getGuid());

        if(requester == null)
            return;

        Optional<LoggedGameUser> optional = requester.getLoggedGameUser();

        if(optional.isPresent()) {

            LoggedGameUser gameUser = optional.get();

            if(gameUser.getViewing() != -1) {

                List<LoggedGameUser> viewers = LoginService.getInstance().getViewers(gameUser.getViewing());

                ResponseGalaxyBroadcastPacket leaveBroadcast = new ResponseGalaxyBroadcastPacket();

                leaveBroadcast.setGuid(requester.getGuid());
                leaveBroadcast.setUserId(requester.getUserId());
                leaveBroadcast.getName().value(requester.getUsername());
                leaveBroadcast.setKind(1);

                for(LoggedGameUser viewer : viewers)
                    if(viewer != gameUser)
                        viewer.getSmartServer().send(leaveBroadcast);

            }

            gameUser.setViewing(user.getUserId());

            List<LoggedGameUser> viewers = LoginService.getInstance().getViewers(gameUser.getViewing());

            ResponseGalaxyBroadcastPacket enterBroadcast = new ResponseGalaxyBroadcastPacket();

            enterBroadcast.setGuid(requester.getGuid());
            enterBroadcast.setUserId(requester.getUserId());
            enterBroadcast.getName().value(requester.getUsername());
            enterBroadcast.setKind(0);

            for(LoggedGameUser viewer : viewers)
                if(viewer != gameUser)
                    viewer.getSmartServer().send(enterBroadcast);

        }

        // ResponseBuildInfoPacket MSG_RESP_BUILDINFO [1]
        packet.getSmartServer().send(getBuildInfoPacket(packet.getGuid(), user));

        List<Fleet> fleets = PacketService.getFleetRepository().findAllByGalaxyId(user.getPlanet().getPosition().galaxyId());

        // ResponseGalaxyShip MSG_RESP_GALAXYSHIP [2]
        if(packet.getGuid() == user.getGuid() && fleets != null && fleets.size() > 0) // todo friends
            packet.getSmartServer().send(getGalaxyShipInfo(user, fleets));

    }

    private ResponseGalaxyShipPacket getGalaxyShipInfo(User user, List<Fleet> fleets) {

        ResponseGalaxyShipPacket response = new ResponseGalaxyShipPacket();

        response.setDataLen((short) fleets.size());
        response.setGalaxyMapId((short) 0);

        response.setGalaxyId(user.getPlanet().getPosition().galaxyId());
        response.setFleets(new ArrayList<>());

        for(Fleet fleet : fleets) {

            GalaxyFleetInfo fleetInfo = GalaxyFleetInfo.builder()
                    .shipTeamId(fleet.getShipTeamId())
                    .shipNum(fleet.ships())
                    .bodyId((short) fleet.getBodyId())
                    .reserve((short) 0)
                    .direction((char) fleet.getDirection())
                    .posX((char) fleet.getPosX())
                    .posY((char) fleet.getPosY())
                    .owner((char) (user.getGuid() == fleet.getGuid() ? 2 : 0))
                    .build();

            response.getFleets().add(fleetInfo);

        }

        return response;

    }

    private ResponseBuildInfoPacket getBuildInfoPacket(int requester, User user) {

        ResponseBuildInfoPacket packet = new ResponseBuildInfoPacket();

        packet.setGalaxyMapId(0);
        packet.setGalaxyId(user.getPlanet().getPosition().galaxyId());
        packet.setViewFlag((char) user.getViewFlag(requester));
        packet.setConsortiaLeader((short) 0);
        packet.setStarType((char) 0);
        packet.setBuildInfoList(UserService.getInstance().getBuilds(user)); // getDebugBase() or getInitialBase()
        packet.setDataLen(packet.getBuildInfoList().size());

        return packet;

    }

    /**
     *
     * A base with at least one of each
     * building in max level.
     *
     * @return
     * todo
     */
    public List<BuildInfo> getDebugBase() {

        List<BuildInfo> list = new ArrayList<>();

        list.add(new BuildInfo(0, 1784, 945, 0, 1, 10));
        list.add(new BuildInfo(0, 1008, 1334, 1, 2, 10));
        list.add(new BuildInfo(0, 902, 503, 2, 3, 14));
        list.add(new BuildInfo(0, 188, 863, 3, 0, 8));
        list.add(new BuildInfo(0, 12, 12, 4, 13, 8));
        list.add(new BuildInfo(0, 1075, 605, 5, 4, 15));
        list.add(new BuildInfo(0, 901, 1391, 6, 2, 10));
        list.add(new BuildInfo(0, 794, 1335, 7, 2, 10));
        list.add(new BuildInfo(0, 893, 1283, 8, 2, 10));
        list.add(new BuildInfo(0, 686, 1280, 9, 2, 10));
        list.add(new BuildInfo(0, 794, 1224, 10, 2, 10));
        list.add(new BuildInfo(0, 1686, 998, 11, 1, 10));
        list.add(new BuildInfo(0, 1678, 895, 12, 1, 10));
        list.add(new BuildInfo(0, 1581, 948, 13, 1, 10));
        list.add(new BuildInfo(0, 1572, 842, 14, 1, 10));
        list.add(new BuildInfo(0, 1473, 895, 15, 1, 10));
        list.add(new BuildInfo(0, 1083, 1246, 16, 9, 7));
        list.add(new BuildInfo(0, 945, 1176, 17, 10, 7));
        list.add(new BuildInfo(0, 802, 554, 18, 3, 10));
        list.add(new BuildInfo(0, 697, 607, 19, 3, 10));
        list.add(new BuildInfo(0, 1006, 554, 20, 3, 10));
        list.add(new BuildInfo(0, 906, 605, 21, 3, 10));
        list.add(new BuildInfo(0, 803, 654, 22, 3, 10));
        list.add(new BuildInfo(0, 1219, 1175, 23, 11, 6));
        list.add(new BuildInfo(0, 1088, 1098, 24, 7, 15));
        list.add(new BuildInfo(0, 588, 659, 25, 3, 10));
        list.add(new BuildInfo(0, 697, 708, 26, 3, 10));
        list.add(new BuildInfo(0, 487, 711, 27, 3, 10));
        list.add(new BuildInfo(0, 593, 761, 28, 3, 10));
        list.add(new BuildInfo(0, 386, 762, 29, 3, 10));
        list.add(new BuildInfo(0, 491, 816, 30, 3, 10));
        list.add(new BuildInfo(0, 584, 1227, 31, 2, 10));
        list.add(new BuildInfo(0, 688, 1174, 32, 2, 15));
        list.add(new BuildInfo(0, 1370, 844, 33, 1, 10));
        list.add(new BuildInfo(0, 1467, 788, 34, 1, 15));
        list.add(new BuildInfo(0, 808, 1108, 35, 6, 6));
        list.add(new BuildInfo(0, 1367, 1099, 36, 14, 6));
        list.add(new BuildInfo(0, 1230, 1031, 37, 5, 7));
        list.add(new BuildInfo(0, 927, 1019, 38, 12, 6));
        list.add(new BuildInfo(0, 1104, 955, 39, 30, 3));
        list.add(new BuildInfo(0, 13, 0, 40, 15, 0));
        list.add(new BuildInfo(0, 11, 0, 41, 16, 0));
        list.add(new BuildInfo(0, 12, 0, 42, 15, 0));
        list.add(new BuildInfo(0, 444, 1135, 43, 8, 0));
        list.add(new BuildInfo(0, 616, 1098, 44, 23, 0));
        list.add(new BuildInfo(0, 235, 991, 45, 26, 0));
        list.add(new BuildInfo(0, 57, 965, 46, 20, 0));
        list.add(new BuildInfo(0, 152, 1005, 47, 21, 0));
        list.add(new BuildInfo(0, 458, 938, 48, 24, 0));
        list.add(new BuildInfo(0, 561, 971, 49, 19, 0));
        list.add(new BuildInfo(0, 752, 1056, 50, 22, 0));
        list.add(new BuildInfo(0, 1357, 723, 51, 31, 0));
        list.add(new BuildInfo(0, 729, 882, 52, 25, 0));
        list.add(new BuildInfo(0, 732, 776, 53, 28, 0));
        list.add(new BuildInfo(0, 1262, 677, 54, 32, 0));
        list.add(new BuildInfo(0, 1219, 750, 55, 29, 0));
        list.add(new BuildInfo(0, 593, 848, 56, 27, 0));
        list.add(new BuildInfo(0, 9, 0, 57, 15, 0));
        list.add(new BuildInfo(0, 10, 0, 58, 15, 0));
        list.add(new BuildInfo(0, 15, 0, 59, 15, 0));
        list.add(new BuildInfo(0, 7, 0, 60, 15, 0));
        list.add(new BuildInfo(0, 8, 0, 61, 16, 0));
        list.add(new BuildInfo(0, 14, 0, 62, 16, 0));
        list.add(new BuildInfo(0, 16, 0, 63, 17, 0));
        list.add(new BuildInfo(0, 6, 0, 64, 17, 0));

        return list;

    }

    /**
     *
     * The initial game default base.
     *
     * @return
     * todo
     */
    public List<BuildInfo> getInitialBase() {

        List<BuildInfo> list = new ArrayList<>();

        list.add(new BuildInfo(0, 693, 886, 0, 1, 0));
        list.add(new BuildInfo(0, 914, 962, 1, 2, 0));
        list.add(new BuildInfo(0, 1136, 982, 2, 3, 0));
        list.add(new BuildInfo(0, 1082, 802, 3, 0, 0));
        list.add(new BuildInfo(0, 12, 12, 4, 13, 0));
        list.add(new BuildInfo(0, 831, 774, 5, 4, 0));

        return list;

    }

}
