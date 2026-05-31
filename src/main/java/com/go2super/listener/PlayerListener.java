package com.go2super.listener;

import com.go2super.database.entity.Planet;
import com.go2super.database.entity.ShipModel;
import com.go2super.database.entity.User;
import com.go2super.database.entity.sub.*;
import com.go2super.logger.BotLogger;
import com.go2super.obj.game.*;
import com.go2super.obj.utility.*;
import com.go2super.packet.PacketListener;
import com.go2super.packet.PacketProcessor;
import com.go2super.packet.boot.ResponsePlayerResourcePacket;
import com.go2super.packet.chat.RequestLookupUserInfoPacket;
import com.go2super.packet.chat.RequestUserInfoPacket;
import com.go2super.packet.chat.ResponseLookupUserInfoPacket;
import com.go2super.packet.chat.ResponseUserInfoPacket;
import com.go2super.packet.commander.ResponseCommanderBaseInfoPacket;
import com.go2super.packet.construction.RequestStorageResourcePacket;
import com.go2super.packet.construction.ResponseStorageResourcePacket;
import com.go2super.packet.instance.ResponseEctypePassPacket;
import com.go2super.packet.map.ResponseMapBlockPacket;
import com.go2super.packet.science.ResponseTechInfoPacket;
import com.go2super.packet.science.ResponseTechUpgradeInfoPacket;
import com.go2super.packet.ship.ResponseShipBodyInfoPacket;
import com.go2super.packet.ship.ResponseShipModelInfoPacket;
import com.go2super.packet.social.RequestFriendInfoPacket;
import com.go2super.packet.social.ResponseFriendInfoPacket;
import com.go2super.packet.task.ResponseTaskInfoPacket;
import com.go2super.packet.upgrade.ResponseShipBodyUpgradeInfoPacket;
import com.go2super.service.CommanderService;
import com.go2super.service.GalaxyService;
import com.go2super.service.PacketService;
import com.go2super.packet.boot.RequestPlayerInfoPacket;
import com.go2super.packet.boot.ResponseRoleInfo;
import com.go2super.packet.construction.ResponseBuildInfoPacket;
import com.go2super.packet.props.ResponsePropsInfoPacket;
import com.go2super.service.UserService;
import lombok.SneakyThrows;

import java.util.List;

public class PlayerListener implements PacketListener {

    @SneakyThrows
    @PacketProcessor
    public void onRequestPlayerInfo(RequestPlayerInfoPacket packet) {

        BotLogger.dev("Player Request PKT - GUID = [" + packet.getGuid() + "]");

        User user = PacketService.getUserRepository().findByGuid(packet.getGuid());

        if(user == null)
            return;

        // ResponseMapBlockPacket MSG_RESP_MAPBLOCK [1]
        packet.getSmartServer().send(getMapPacket());

        // ResponseShipModelInfoPacket MSG_RESP_SHIPMODELINFO [2]
        packet.getSmartServer().send(getShipModelInfoPacket(user));

        // ResponseTechInfoPacket MSG_RESP_TECHINFO [3]
        packet.getSmartServer().send(getTechInfoPacket());

        // ResponseTechUpgradeInfoPacket MSG_RESP_TECHUPGRADEINFO [4]
        packet.getSmartServer().send(getTechUpgradeInfoPacket());

        // ResponsePropsInfoPacket MSG_RESP_PROPSINFO [5]
        packet.getSmartServer().send(getPropsInfoPacket(user));

        // ResponseShipBodyInfoPacket MSG_RESP_SHIPBODYINFO [6]
        packet.getSmartServer().send(getShipBodyInfoPacket(user));

        // ResponseShipBodyUpgradeInfoPacket MSG_RESP_SHIPBODYUPGRADEINFO [7]
        packet.getSmartServer().send(getShipBodyUpgradeInfoPacket(user));

        // ResponseCommanderBaseInfoPacket MSG_RESP_COMMANDERBASEINFO [8]
        packet.getSmartServer().send(getCommanderBaseInfoPacket(user));

        // ResponseTaskInfoPacket MSG_RESP_TASKINFO [9]
        packet.getSmartServer().send(getTaskInfoPacket(user));

        // ResponseRoleInfoPacket MSG_ROLE_INFO [10]
        packet.getSmartServer().send(getRoleInfoPacket(user));

        // ResponsePlayerResourcePacket MSG_RESP_PLAYERRESOURCE [11]
        packet.getSmartServer().send(getPlayerResourcePacket(user));

        // ResponseBuildInfoPacket MSG_RESP_BUILDINFO [11b] — own base layout for planet scene
        packet.getSmartServer().send(getBuildInfoPacket(user));

        // ResponseCreateShipInfoPacket MSG_RESP_CREATESHIPINFO [12]
        // todo packet.getSmartServer().send(getCreateShipInfoPacket(user));

        // ResponseTimeQueuePacket MSG_RESP_TIMEQUEUE [13]
        if(!user.getStats().getBuffs().isEmpty())
            packet.getSmartServer().send(user.getQueuesAsPacket());

        // ResponseOnlineAwardPacket MSG_RESP_ONLINEAWARD [14]
        // todo packet.getSmartServer().send(getOnlineAwardPacket(user));

        // ResponseNewMailNoticePacket MSG_RESP_NEWEMAILNOTICE [15]
        // todo packet.getSmartServer().send(getNewEmailNoticePacket(user));

        // ResponseEctypePassPacket MSG_RESP_ECTYPEPASS [16]
        packet.getSmartServer().send(getEctypePassPacket(user));

    }

    @PacketProcessor
    public void onLookupUserInfo(RequestLookupUserInfoPacket packet) {

        GalaxyTile tile = new GalaxyTile(packet.getObjGalaxyId());
        Planet planet = GalaxyService.getInstance().getPlanet(tile);
        ResponseLookupUserInfoPacket response = new ResponseLookupUserInfoPacket();

        if(planet == null) {

            User user = null;

            if(packet.getObjGuid() != 0 && packet.getObjGuid() != -1)
                user = UserService.getInstance().getUserRepository().findByGuid(packet.getObjGuid());

            if(user == null) {

                response.setPosY(0);
                response.setPosX(0);
                response.setGalaxyId(-1);
                response.setType(-1);
                response.setGuid(-1);
                response.getUserName().value("");

                packet.getSmartServer().send(packet);

                return;

            }

            planet = user.getPlanet();

        }

        ResponseUserInfoPacket lookup;

        switch(planet.getType()) {

            case USER_PLANET:
                lookup = getUserPlanetInfo(planet);
                break;

            case HUMAROID_PLANET:
                lookup = getHumaroidUserInfo(planet);
                break;

            case RESOURCES_PLANET:
                lookup = getRBPUserInfo(planet);
                break;

            default:
                throw new IllegalStateException("Unexpected value: " + planet.getType());

        }

        response.setGuid(lookup.getGuid());
        response.setUserId(lookup.getUserId());
        response.setUserName(lookup.getUserName());

        response.setPosX(lookup.getPosX());
        response.setPosY(lookup.getPosY());
        response.setGalaxyId(lookup.getGalaxyId());
        response.setType(planet.getType().getCode());

        packet.getSmartServer().send(response);

    }

    @PacketProcessor
    public void onUserInfo(RequestUserInfoPacket packet) {

        Planet planet = null;

        if(packet.getObjGuid() >= 0) {

            User user = PacketService.getUserRepository().findByGuid(packet.getObjGuid());
            planet = user.getPlanet();

        } else if(packet.getObjGalaxyId() >= 0) {

            GalaxyTile tile = new GalaxyTile(packet.getObjGalaxyId());
            planet = GalaxyService.getInstance().getPlanet(tile);

        }

        if(planet == null)
            return;

        ResponseUserInfoPacket response;

        switch(planet.getType()) {

            case USER_PLANET:
                response = getUserPlanetInfo(planet);
                break;

            case HUMAROID_PLANET:
                response = getHumaroidUserInfo(planet);
                break;

            case RESOURCES_PLANET:
                response = getRBPUserInfo(planet);
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + planet.getType());
        }

        packet.getSmartServer().send(response);

    }

    @PacketProcessor
    public void onFriendInfo(RequestFriendInfoPacket packet) {

        User user = null;

        if(packet.getObjGuid() >= 0)
            user = PacketService.getUserRepository().findByGuid(packet.getObjGuid());
        else if(packet.getObjUserId() >= 0)
            user = PacketService.getUserRepository().findByUserId(packet.getObjUserId());

        if(user == null)
            return;

        ResponseFriendInfoPacket response = new ResponseFriendInfoPacket();

        response.setGuid(user.getGuid());
        response.setUserId(user.getUserId());
        response.setFightFlag((int) 0);
        response.setStarType((int) 0);
        response.setLevelId(user.getStats().getLevel());
        response.setExp(user.getStats().getExp());
        response.setGalaxyMapId((int) 0);
        response.setGalaxyId(user.getPlanet().getPosition().galaxyId());

        packet.getSmartServer().send(response);

    }

    @PacketProcessor
    public void onStorageResource(RequestStorageResourcePacket packet) {

        User user = PacketService.getUserRepository().findByGuid(packet.getGuid());
        UserStorage storage = user.getStorage();

        ResponseStorageResourcePacket response = new ResponseStorageResourcePacket();

        response.setGas(storage.getHe3());
        response.setMetal(storage.getMetal());
        response.setMoney(storage.getGold());

        response.setStorageGas(storage.getMaxHe3());
        response.setStorageMetal(storage.getMaxMetal());
        response.setStorageMoney(storage.getMaxGold());

        packet.getSmartServer().send(response);
        UserService.getInstance().updateStats(user);

    }

    private ResponseUserInfoPacket getUserPlanetInfo(Planet planet) {

        UserPlanet userPlanet = (UserPlanet) planet;
        User user = userPlanet.getUser();

        ResponseUserInfoPacket packet = new ResponseUserInfoPacket();

        packet.setGuid(user.getGuid());
        packet.setUserId(userPlanet.getUserId());

        packet.getUserName().value(user.getUsername());
        packet.getConsortia().value("Unknow");

        packet.setRankId(user.getAttackPowerRank() + 1);
        packet.setPosX(userPlanet.getPosition().getX());
        packet.setPosY(userPlanet.getPosition().getY());
        packet.setPeaceTime(0);
        packet.setGalaxyId(userPlanet.getPosition().galaxyId());

        packet.setSpaceLevel((char) user.getSpaceStationLevel());
        packet.setCityLevel((char) user.getCityLevel());

        packet.setLevelId(UnsignedChar.of(user.getStats().getLevel()));
        packet.setMatchLevel(UnsignedChar.of(user.getCurrentLeague()));

        packet.setPassMaxEctype(user.getEctypeNum());
        packet.setConsortiaId(0);
        packet.setPassInsertFlagTime(0);
        packet.setInsertFlagConsortiaId(0);
        packet.setInsertFlagConsortia(SmartString.of("", 32));

        return packet;

    }

    private ResponseUserInfoPacket getHumaroidUserInfo(Planet planet) {

        HumaroidPlanet humaPlanet = (HumaroidPlanet) planet;
        ResponseUserInfoPacket packet = new ResponseUserInfoPacket();

        packet.setGuid(-1);
        packet.setUserId(planet.getUserId());

        packet.getUserName().value("Humaroid");
        packet.getConsortia().value("");

        packet.setRankId(0);
        packet.setPosX(planet.getPosition().getX());
        packet.setPosY(planet.getPosition().getY());
        packet.setPeaceTime(10800);
        packet.setGalaxyId(planet.getPosition().galaxyId());

        packet.setSpaceLevel((char) 0);
        packet.setCityLevel((char) 0);

        packet.setLevelId(UnsignedChar.of(humaPlanet.getCurrentLevel()));
        packet.setMatchLevel(UnsignedChar.of(0));

        packet.setPassMaxEctype(0);
        packet.setConsortiaId(0);
        packet.setPassInsertFlagTime(0);
        packet.setInsertFlagConsortiaId(0);
        packet.setInsertFlagConsortia(SmartString.of("", 32));

        return packet;

    }

    private ResponseUserInfoPacket getRBPUserInfo(Planet planet) {

        ResourcePlanet resourcePlanet = (ResourcePlanet) planet;
        ResponseUserInfoPacket packet = new ResponseUserInfoPacket();

        packet.setGuid(-1);
        packet.setUserId(planet.getUserId());

        packet.getUserName().value("Resource Bonus Planet");
        packet.getConsortia().value("");

        packet.setRankId(0);
        packet.setPosX(planet.getPosition().getX());
        packet.setPosY(planet.getPosition().getY());
        packet.setPeaceTime(10800);
        packet.setGalaxyId(planet.getPosition().galaxyId());

        packet.setSpaceLevel((char) 9);
        packet.setCityLevel((char) 0);

        packet.setLevelId(UnsignedChar.of(0));
        packet.setMatchLevel(UnsignedChar.of(0));

        packet.setPassMaxEctype(0);
        packet.setConsortiaId(0);
        packet.setPassInsertFlagTime(0);
        packet.setInsertFlagConsortiaId(0);
        packet.setInsertFlagConsortia(SmartString.of("", 32));

        return packet;

    }

    public void techUpgradeInfo(List<TechUpgradeInfo> techUpgradeInfo) {

        techUpgradeInfo.add(new TechUpgradeInfo(0, 0, 0));
        techUpgradeInfo.add(new TechUpgradeInfo(0, 0, 0));
        techUpgradeInfo.add(new TechUpgradeInfo(0, 0, 0));
        techUpgradeInfo.add(new TechUpgradeInfo(0, 0, 0));
        techUpgradeInfo.add(new TechUpgradeInfo(0, 0, 0));
        techUpgradeInfo.add(new TechUpgradeInfo(-1, 24227, 0));
        techUpgradeInfo.add(new TechUpgradeInfo(-1710747216, 0, 0));
        techUpgradeInfo.add(new TechUpgradeInfo(1643976, 0, 0));
        techUpgradeInfo.add(new TechUpgradeInfo(0, 0, 0));
        techUpgradeInfo.add(new TechUpgradeInfo(35045152, 0, 0));
        techUpgradeInfo.add(new TechUpgradeInfo(1015837328, 0, 0));
        techUpgradeInfo.add(new TechUpgradeInfo(-2, -1, 0));
        techUpgradeInfo.add(new TechUpgradeInfo(190589600, 0, 0));
        techUpgradeInfo.add(new TechUpgradeInfo(35045152, 0, 0));
        techUpgradeInfo.add(new TechUpgradeInfo(1015837328, 0, 0));
        techUpgradeInfo.add(new TechUpgradeInfo(190745472, 0, 0));
        techUpgradeInfo.add(new TechUpgradeInfo(-1710743504, 0, 0));
        techUpgradeInfo.add(new TechUpgradeInfo(190745472, 0, 0));
        techUpgradeInfo.add(new TechUpgradeInfo(-1710821344, 0, 0));
        techUpgradeInfo.add(new TechUpgradeInfo(-627261515, 9996, 0));

    }

    public void techInfo(List<TechInfo> techInfo) {

        for(int i = 0; i < 16; i++)
            techInfo.add(new TechInfo(i, -1));

        for(int i = 32; i < 48; i++)
            techInfo.add(new TechInfo(i, -1));

        for(int i = 70; i < 89; i++)
            techInfo.add(new TechInfo(i, -1));

        for(int i = 100; i < 110; i++)
            techInfo.add(new TechInfo(i, -1));

        for(int i = 0; i < 87; i++)
            techInfo.add(new TechInfo(0, 0));

        /*techInfo.add(new TechInfo(0, -1));
        techInfo.add(new TechInfo(1, 4));
        techInfo.add(new TechInfo(2, 1));
        techInfo.add(new TechInfo(3, 4));
        techInfo.add(new TechInfo(4, 4));
        techInfo.add(new TechInfo(5, 0));
        techInfo.add(new TechInfo(6, 2));
        techInfo.add(new TechInfo(7, 2));
        techInfo.add(new TechInfo(8, 2));
        techInfo.add(new TechInfo(9, 0));
        techInfo.add(new TechInfo(10, 4));
        techInfo.add(new TechInfo(11, 2));
        techInfo.add(new TechInfo(12, 4));
        techInfo.add(new TechInfo(13, 0));
        techInfo.add(new TechInfo(14, 1));
        techInfo.add(new TechInfo(15, 2));
        techInfo.add(new TechInfo(32, 9));
        techInfo.add(new TechInfo(33, 4));
        techInfo.add(new TechInfo(34, 4));
        techInfo.add(new TechInfo(35, 1));
        techInfo.add(new TechInfo(36, 3));
        techInfo.add(new TechInfo(37, 3));
        techInfo.add(new TechInfo(38, 4));
        techInfo.add(new TechInfo(39, 0));
        techInfo.add(new TechInfo(40, 3));
        techInfo.add(new TechInfo(41, 1));
        techInfo.add(new TechInfo(42, 2));
        techInfo.add(new TechInfo(43, 2));
        techInfo.add(new TechInfo(44, 3));
        techInfo.add(new TechInfo(45, 0));
        techInfo.add(new TechInfo(46, 0));
        techInfo.add(new TechInfo(47, 3));
        techInfo.add(new TechInfo(70, 1));
        techInfo.add(new TechInfo(71, 4));
        techInfo.add(new TechInfo(72, 2));
        techInfo.add(new TechInfo(73, 1));
        techInfo.add(new TechInfo(74, 2));
        techInfo.add(new TechInfo(75, 1));
        techInfo.add(new TechInfo(76, 1));
        techInfo.add(new TechInfo(77, 2));
        techInfo.add(new TechInfo(78, 1));
        techInfo.add(new TechInfo(79, 2));
        techInfo.add(new TechInfo(80, 4));
        techInfo.add(new TechInfo(81, 2));
        techInfo.add(new TechInfo(82, 1));
        techInfo.add(new TechInfo(83, 2));
        techInfo.add(new TechInfo(84, 1));
        techInfo.add(new TechInfo(85, 1));
        techInfo.add(new TechInfo(86, 2));
        techInfo.add(new TechInfo(87, 1));
        techInfo.add(new TechInfo(88, 2));
        techInfo.add(new TechInfo(100, 0));
        techInfo.add(new TechInfo(101, 9));
        techInfo.add(new TechInfo(102, 9));
        techInfo.add(new TechInfo(103, 2));
        techInfo.add(new TechInfo(104, 4));
        techInfo.add(new TechInfo(105, 0));
        techInfo.add(new TechInfo(106, 5));
        techInfo.add(new TechInfo(107, 9));
        techInfo.add(new TechInfo(108, 5));
        techInfo.add(new TechInfo(109, 9));
        techInfo.add(new TechInfo(110, 1));
        techInfo.add(new TechInfo(239, 239));
        techInfo.add(new TechInfo(239, 239));
        techInfo.add(new TechInfo(239, 212));
        techInfo.add(new TechInfo(239, 239));
        techInfo.add(new TechInfo(239, 239));
        techInfo.add(new TechInfo(239, 239));
        techInfo.add(new TechInfo(239, 239));
        techInfo.add(new TechInfo(357, 357));
        techInfo.add(new TechInfo(5576, 25));
        techInfo.add(new TechInfo(17747, 22872));
        techInfo.add(new TechInfo(16716, 20053));
        techInfo.add(new TechInfo(18499, 21061));
        techInfo.add(new TechInfo(25856, 25856));
        techInfo.add(new TechInfo(-27648, 177));
        techInfo.add(new TechInfo(-31535, -18992));
        techInfo.add(new TechInfo(-16944, -32303));
        techInfo.add(new TechInfo(-18992, 209));
        techInfo.add(new TechInfo(29, 276));
        techInfo.add(new TechInfo(469, 426));
        techInfo.add(new TechInfo(240, 429));
        techInfo.add(new TechInfo(401, 328));
        techInfo.add(new TechInfo(218, 215));
        techInfo.add(new TechInfo(212, 209));
        techInfo.add(new TechInfo(206, 487));
        techInfo.add(new TechInfo(487, 487));
        techInfo.add(new TechInfo(487, 487));
        techInfo.add(new TechInfo(487, 487));
        techInfo.add(new TechInfo(487, 487));
        techInfo.add(new TechInfo(487, 487));
        techInfo.add(new TechInfo(487, 176));
        techInfo.add(new TechInfo(182, 432));
        techInfo.add(new TechInfo(432, 233));
        techInfo.add(new TechInfo(361, 478));
        techInfo.add(new TechInfo(477, 477));
        techInfo.add(new TechInfo(16, 238));
        techInfo.add(new TechInfo(472, 238));
        techInfo.add(new TechInfo(238, 238));
        techInfo.add(new TechInfo(238, 238));
        techInfo.add(new TechInfo(238, 238));
        techInfo.add(new TechInfo(238, 238));
        techInfo.add(new TechInfo(238, 238));
        techInfo.add(new TechInfo(238, 238));
        techInfo.add(new TechInfo(238, 238));
        techInfo.add(new TechInfo(10013, 25));
        techInfo.add(new TechInfo(30789, 25711));
        techInfo.add(new TechInfo(24937, 107));
        techInfo.add(new TechInfo(29285, 28160));
        techInfo.add(new TechInfo(19041, 13140));
        techInfo.add(new TechInfo(29184, 0));
        techInfo.add(new TechInfo(0, 0));
        techInfo.add(new TechInfo(-8071, -1));
        techInfo.add(new TechInfo(0, 0));
        techInfo.add(new TechInfo(41, 308));
        techInfo.add(new TechInfo(469, 426));
        techInfo.add(new TechInfo(240, 206));
        techInfo.add(new TechInfo(212, 218));
        techInfo.add(new TechInfo(429, 167));
        techInfo.add(new TechInfo(182, 360));
        techInfo.add(new TechInfo(432, 432));
        techInfo.add(new TechInfo(432, 432));
        techInfo.add(new TechInfo(432, 432));
        techInfo.add(new TechInfo(432, 432));
        techInfo.add(new TechInfo(432, 432));
        techInfo.add(new TechInfo(358, 358));
        techInfo.add(new TechInfo(358, 358));
        techInfo.add(new TechInfo(358, 472));
        techInfo.add(new TechInfo(472, 472));
        techInfo.add(new TechInfo(472, 472));
        techInfo.add(new TechInfo(472, 472));
        techInfo.add(new TechInfo(472, 472));
        techInfo.add(new TechInfo(472, 472));
        techInfo.add(new TechInfo(472, 472));
        techInfo.add(new TechInfo(472, 472));
        techInfo.add(new TechInfo(472, 238));
        techInfo.add(new TechInfo(358, 358));
        techInfo.add(new TechInfo(182, 397));
        techInfo.add(new TechInfo(238, 238));
        techInfo.add(new TechInfo(238, 238));
        techInfo.add(new TechInfo(17170, 25));
        techInfo.add(new TechInfo(0, 0));
        techInfo.add(new TechInfo(0, 0));
        techInfo.add(new TechInfo(0, 0));
        techInfo.add(new TechInfo(0, 0));
        techInfo.add(new TechInfo(0, 0));
        techInfo.add(new TechInfo(0, 0));
        techInfo.add(new TechInfo(0, 0));
        techInfo.add(new TechInfo(0, 0));
        techInfo.add(new TechInfo(0, 0));*/

    }

    private ResponseShipModelInfoPacket getShipModelInfoDelPacket(User user) throws Exception {
        throw new Exception("abc");
    }

    private ResponseShipModelInfoPacket getShipModelInfoPacket(User user) {

        ResponseShipModelInfoPacket packet = new ResponseShipModelInfoPacket();

        List<ShipModel> shipModels = PacketService.getShipModelRepository().findAllByGuidAndDeleted(user.getGuid(), false);
        shipModels.add(0, PacketService.getShipModel(0));

        packet.setDataLen(UnsignedShort.of(shipModels.size()));

        for(ShipModel model : shipModels)
            packet.getShipModelInfoList().add(
                    ShipModelInfo.of(model.getName(), model.partNum(), model.getShipModelId() == 0 ? 1 : 0, model.getBodyId(), model.partArray(), model.getShipModelId())
            );

        return packet;

    }

    private ResponseTechUpgradeInfoPacket getTechUpgradeInfoPacket() {

        ResponseTechUpgradeInfoPacket packet = new ResponseTechUpgradeInfoPacket();
        List<TechUpgradeInfo> techUpgradeInfo = packet.getTechUpgradeInfoList();

        packet.setIncTechPercent((short) 66);
        packet.setDataLen(0);

        techUpgradeInfo(techUpgradeInfo);
        return packet;

    }

    private ResponseTechInfoPacket getTechInfoPacket() {

        ResponseTechInfoPacket packet = new ResponseTechInfoPacket();
        List<TechInfo> techInfo = packet.getTechInfoList();

        packet.setDataLen(techInfo.size());
        techInfo(techInfo);
        return packet;

    }

    private ResponsePropsInfoPacket getPropsInfoPacket(User user) {

        UserInventory inventory = user.getInventory();
        ResponsePropsInfoPacket packet = new ResponsePropsInfoPacket();

        packet.setDataLen(inventory.getPropList().size());
        packet.setPropList(inventory.getPropList());

        return packet;

    }

    private ResponseShipBodyInfoPacket getShipBodyInfoPacket(User user) {

        ResponseShipBodyInfoPacket packet = new ResponseShipBodyInfoPacket();

        packet.setBodyNum((short) 14);
        packet.setPartNum((short) 36);

        packet.setBodyId(new ShortArray(new int[]{65,2,32,68,5,35,38,62,8,71,77,74,159,80,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0}));
        packet.setPartId(new ShortArray(new int[]{234,20,92,23,185,95,56,2,59,238,128,239,188,191,164,221,224,131,132,98,135,5,26,167,101,62,104,110,173,194,235,240,265,206,32,68,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,-1,-1,24374,60,0,0,0,0,-1,-1,24374,60,9920,-24882,0,0,5576,25,0,0}));

        return packet;

    }

    private ResponseCommanderBaseInfoPacket getCommanderBaseInfoPacket(User user) {
        return CommanderService.getInstance().getBaseInfoPacket(user);
    }

    private ResponseTaskInfoPacket getTaskInfoPacket(User user) {

        ResponseTaskInfoPacket packet = new ResponseTaskInfoPacket();

        packet.setDataLen((short) 1);
        packet.setAwardLen((short) 1);
        packet.setByteArray(new ByteArray(new int[24]));

        for(int i = 0; i < 100; i++)
            packet.getTaskInfos().add(new TaskInfo(0, 0, 0, 0, 0, 0));

        return packet;

    }

    private ResponseShipBodyUpgradeInfoPacket getShipBodyUpgradeInfoPacket(User user) {

        ResponseShipBodyUpgradeInfoPacket packet = new ResponseShipBodyUpgradeInfoPacket();

        packet.setIncUpgradePercent(24);
        packet.setBodyNum((short) 0);
        packet.setPartNum((short) 0);

        packet.getBodyId().add(new ShipBodyInfo(0, 0));
        packet.getPartId().add(new ShipBodyInfo(0, 0));

        return packet;

    }

    private ResponsePlayerResourcePacket getPlayerResourcePacket(User user) {

        ResponsePlayerResourcePacket packet = new ResponsePlayerResourcePacket();

        packet.setUserGas(UnsignedInteger.of(user.getResources().getHe3()));
        packet.setUserMetal(UnsignedInteger.of(user.getResources().getMetal()));
        packet.setUserMoney(UnsignedInteger.of(user.getResources().getGold()));

        packet.setCredit(UnsignedInteger.of(user.getResources().getMallPoints()));
        packet.setLevel(user.getStats().getLevel());
        packet.setExp(user.getStats().getExp());
        packet.setCoins(user.getResources().getVouchers());
        packet.setOutGas(user.getStorage().getHe3Production());
        packet.setOutMetal(user.getStorage().getMetalProduction());
        packet.setOutMoney(user.getStorage().getGoldProduction());
        packet.setMaxSpValue(user.getStats().getMaxSp());
        packet.setSpValue(user.getStats().getSp());
        packet.setMoneyBuyNum(0);
        packet.setDefyEctypeNum(0);
        packet.setMatchCount(0);
        packet.setTollGate(0);
        packet.setReserve(0);

        UserService.getInstance().updateStats(user);
        return packet;

    }

    private ResponseRoleInfo getRoleInfoPacket(User user) {

        ResponseRoleInfo packet = new ResponseRoleInfo();

        packet.setGMapId(user.getGMapId());
        packet.setGId(GalaxyService.getInstance().getUserPlanet(user).getPosition().galaxyId());
        packet.setConsortiaId(user.getConsortiaId());
        packet.setPropsPack(user.getInventory().getMaximumStacks());
        packet.setPropsCorpPack(0);
        packet.setConsortiaJob((char) user.getConsortiaJob());
        packet.setConsortiaUnionLevel((char) 0);
        packet.setConsortiaShopLevel((char) 0);
        packet.setGameServerId((char) 0);
        packet.setCard1(100);
        packet.setCardCredit(3);
        packet.setCard2(200);
        packet.setCard3(400);
        packet.setCardUnion(0);
        packet.setChargeFlag(1);
        packet.setAddPackMoney(user.getInventory().getStackPrice());
        packet.setShipSpeedCredit(5);
        packet.setLotteryCredit(5);
        packet.setLotteryStatus(user.getSpins() > 0 ? 0 : 1);
        packet.setConsortiaThrow(10512);
        packet.setConsortiaUnion(0);
        packet.setConsortiaShop(0);
        packet.setName(SmartString.of(user.getUsername(), 32));
        packet.setEctypeNum(0);
        packet.setBadge(user.getResources().getBadge());
        packet.setHonor(user.getResources().getHonor());
        packet.setServerTime(UnsignedInteger.of(new GameDate().getSeconds()));
        packet.setTollGate(0);
        packet.setYear((short) 2020);
        packet.setMonth((char) 11);
        packet.setDay((char) 18);
        packet.setNoviceGuide(0);
        packet.setWarScore(UnsignedInteger.of(130));

        return packet;

    }

    private ResponseEctypePassPacket getEctypePassPacket(User user) {

        ResponseEctypePassPacket packet = new ResponseEctypePassPacket();

        packet.setDataLen(30);
        packet.fill(30);

        return packet;

    }

    private ResponseMapBlockPacket getMapPacket() {

        ResponseMapBlockPacket packet = new ResponseMapBlockPacket();
        packet.setBlockCount(GalaxyService.getInstance().getCurrentZones());
        return packet;

    }

    private ResponseBuildInfoPacket getBuildInfoPacket(User user) {

        ResponseBuildInfoPacket packet = new ResponseBuildInfoPacket();

        packet.setGalaxyMapId(0);
        packet.setGalaxyId(user.getPlanet().getPosition().galaxyId());
        packet.setViewFlag((char) user.getViewFlag(user.getGuid()));
        packet.setConsortiaLeader((short) 0);
        packet.setStarType((char) Math.max(0, Math.min(2, user.getGround())));
        packet.setBuildInfoList(UserService.getInstance().getBuilds(user));
        packet.setDataLen(packet.getBuildInfoList().size());

        return packet;

    }

}