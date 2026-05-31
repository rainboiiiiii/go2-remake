package com.go2super.listener;

import com.go2super.database.entity.Commander;
import com.go2super.database.entity.Fleet;
import com.go2super.database.entity.ShipModel;
import com.go2super.database.entity.User;
import com.go2super.database.entity.sub.UserShips;
import com.go2super.logger.BotLogger;
import com.go2super.obj.game.GalaxyFleetInfo;
import com.go2super.obj.game.Prop;
import com.go2super.obj.game.ShipTeamBody;
import com.go2super.obj.game.ShipTeamNum;
import com.go2super.obj.utility.Gem;
import com.go2super.packet.PacketListener;
import com.go2super.packet.PacketProcessor;
import com.go2super.packet.PacketRouter;
import com.go2super.packet.commander.*;
import com.go2super.packet.gems.*;
import com.go2super.packet.ship.ResponseEditShipTeamPacket;
import com.go2super.resources.ResourceManager;
import com.go2super.resources.data.PropData;
import com.go2super.resources.data.props.PropGemData;
import com.go2super.service.CommanderService;
import com.go2super.service.LoginService;
import com.go2super.service.PacketService;
import com.go2super.service.exception.BadGuidException;
import com.go2super.socket.util.MathUtil;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class CommanderListener implements PacketListener {

    @PacketProcessor
    public void onEditFleet(RequestCommanderEditShipTeamPacket packet) {

        User user = PacketService.getUserRepository().findByGuid(packet.getGuid());

        if(user == null)
            return;

        Fleet fleet = PacketService.getFleetRepository().findByShipTeamId(packet.getShipTeamId());

        if(fleet == null || fleet.getGuid() != packet.getGuid())
            return;

        ShipTeamBody oldTeamBody = fleet.getFleetBody();
        ShipTeamBody newTeamBody = packet.getTeamBody();

        UserShips ships = user.getShips();

        int total = 0;
        int body = -1;

        if(newTeamBody.getCells().size() != 9)
            return;

        for(ShipTeamNum oldNum : oldTeamBody.getCells())
            if(oldNum.getNum() != 0 && oldNum.getShipModelId() > -1)
                ships.addShip(oldNum.getShipModelId(), oldNum.getNum());

        for(ShipTeamNum newNum : newTeamBody.getCells()) {

            if(newNum.getShipModelId() <= -1 || newNum.getNum() <= 0)
                continue;

            if(newNum.getNum() > 3000)
                return;

            if(!ships.removeShip(newNum.getShipModelId(), newNum.getNum()))
                return;

            ShipModel model = PacketService.getShipModel(newNum.getShipModelId());

            if(model == null)
                return;

            if(body < model.getBodyId())
                body = model.getBodyId();

            total += newNum.getNum();

        }

        fleet.setFleetBody(newTeamBody);
        fleet.setBodyId(body);

        fleet.setTarget(packet.getTarget());
        fleet.setTargetInterval(packet.getTargetInterval());

        if(total <= 0)
            return;

        System.out.println(packet);

        user.save();
        fleet.save();

        ResponseCommanderEditShipTeamPacket response = new ResponseCommanderEditShipTeamPacket();

        response.setErrorCode(0);
        response.setKind(packet.getKind());

        packet.getSmartServer().send(response);

    }

    @PacketProcessor
    public void onCommanderInfo(RequestCommanderInfoPacket packet) {

        User user = PacketService.getUserRepository().findByGuid(packet.getGuid());

        if(user == null)
            return;

        ResponseCommanderInfoPacket response = CommanderService.getInstance().getInfoPacket(packet.getCommanderId(), packet.getShowType(), user);

        if(response != null)
            packet.getSmartServer().send(response);

    }

    @PacketProcessor
    public void onCommanderInsertStone(RequestCommanderInsertStonePacket packet) throws BadGuidException {

        LoginService.validate(packet, packet.getGuid());
        User user = PacketService.getUserRepository().findByGuid(packet.getGuid());

        if(packet.getGemType() != 0 && packet.getGemType() != 1)
            return;

        if(user == null)
            return;

        if(!(packet.getHoleId() >= 0 && packet.getHoleId() <= 11))
            return;

        Commander commander = user.getCommander(packet.getCommanderId());

        if(commander == null)
            return;

        int maxGems = commander.getLevel().getLevelData().getGem();

        if(maxGems < packet.getHoleId() + 1)
            return;

        List<Integer> gems = commander.getGems();
        final int[] validator = new int[]{ 1, 2, 3, -1, 1, 2, 3, -1, 1, 2, 3, -1 };

        if(gems.size() == 0)
            for(int i = 0; i < 12; i++)
                gems.add(-1);

        if(packet.getGemType() == 0) {

            Prop gem = user.getInventory().getProp(packet.getPropsId());

            if(gem == null)
                return;

            PropData data = gem.getData();

            if(!data.getType().equals("gem"))
                return;

            PropGemData gemData = data.getGemData();

            if(gemData == null)
                return;

            int validation = validator[packet.getHoleId()];

            if(validation != -1 && gemData.getColor() != validation)
                return;

            if(gems.get(packet.getHoleId()) != -1)
                return;

            if(!user.getInventory().removeProp(data.getId(), 1, 0, packet.getLockFlag() == 1))
                return;

            gems.set(packet.getHoleId(), data.getId());

            ResponseCommanderInsertStonePacket response = CommanderService.getInstance().getCommanderInsertStonePacket(packet.getGemType(), packet.getCommanderId(), packet.getHoleId(), packet.getPropsId(), packet.getLockFlag());

            packet.getSmartServer().send(response);
            user.save();
            commander.save();
            return;

        }

        if(!user.getInventory().addProp(gems.get(packet.getHoleId()), 1, 0, true))
            return;

        gems.set(packet.getHoleId(), -1);

        ResponseCommanderInsertStonePacket response = CommanderService.getInstance().getCommanderInsertStonePacket(packet.getGemType(), packet.getCommanderId(), packet.getHoleId(), packet.getPropsId(), 1);

        packet.getSmartServer().send(response);
        user.save();
        commander.save();

    }

    @PacketProcessor
    public void onUnionCommanderCard(RequestUnionCommanderCardPacket packet) throws BadGuidException {

        if(packet.getCard3() == -1 && packet.getCard1() != packet.getCard2())
            return;

        LoginService.validate(packet, packet.getGuid());
        User user = PacketService.getUserRepository().findByGuid(packet.getGuid());

        if(user == null)
            return;

        int mergeCard = packet.getCard1();
        int nextCard = mergeCard + 1;

        PropData mergeData = CommanderService.getInstance().getCommanderPropData(mergeCard);
        PropData nextData = CommanderService.getInstance().getCommanderPropData(nextCard);

        PropData card2 = CommanderService.getInstance().getCommanderPropData(packet.getCard2());

        if(mergeData == null || nextData == null || mergeData != nextData)
            return;

        int stars1 = packet.getCard1() - mergeData.getId();
        int stars2 = packet.getCard2() - card2.getId();

        int resultStars = nextCard - mergeData.getId();

        int chipSuccess = 0;

        if(packet.getGoods() == 925 || packet.getGoods() == 944) {

            Prop chip = user.getInventory().getProp(packet.getGoods());

            if(user.getInventory().removeOneProp(chip, packet.getGoodsLockFlag() == 1))
                chipSuccess += packet.getGoods() == 925 ? 20 : 30;

        }

        int success = 100 + chipSuccess;

        if(resultStars >= 3)
            success -= (resultStars - 2) * 10;

        boolean failed = false;

        if(MathUtil.random(0, 100) > success)
            failed = true;

        if(packet.getCard3() == -1) {

            Prop prop = user.getInventory().getProp(mergeCard);

            if(prop == null || prop.getPropNum() < 2)
                return;

            if(!user.getInventory().removeProp(prop, 2, false))
                return;

            if(!failed)
                if(!user.getInventory().addProp(nextCard, 1, 0, false))
                    return;

            ResponseUnionCommanderCardPacket response = CommanderService.getInstance().getUnionCommanderCardPacket(!failed ? nextCard : -1, packet.getCard1(), packet.getCard2(), packet.getCard3(), packet.getGoods(), packet.getGoodsLockFlag());

            packet.getSmartServer().send(response);
            user.save();

            if(resultStars >= 3) {

                ResponseUnionCommanderCardBroPacket broadcast = CommanderService.getInstance().getUnionCommanderCardBroPacket(user, mergeData.getCommanderData().getSkillId(), resultStars, failed);
                PacketRouter.getInstance().broadcast(broadcast);

            }
            return;

        }


        PropData card3 = CommanderService.getInstance().getCommanderPropData(packet.getCard3());

        if(card2 == null || card3 == null)
            return;

        int stars3 = packet.getCard3() - card3.getId();

        if(stars1 != stars2 || stars1 != stars3)
            return;

        if(card2.getCommanderData().getType() != mergeData.getCommanderData().getType() || card3.getCommanderData().getType() != mergeData.getCommanderData().getType())
            return;

        if(!user.getInventory().removeProp(packet.getCard1(), 1, 0, false))
            return;

        if(!user.getInventory().removeProp(packet.getCard2(), 1, 0, false))
            return;

        if(!user.getInventory().removeProp(packet.getCard3(), 1, 0, false))
            return;

        if(!failed)
            if(!user.getInventory().addProp(nextCard, 1, 0, false))
                return;

        ResponseUnionCommanderCardPacket response = CommanderService.getInstance().getUnionCommanderCardPacket(!failed ? nextCard : -1, packet.getCard1(), packet.getCard2(), packet.getCard3(), packet.getGoods(), packet.getGoodsLockFlag());

        packet.getSmartServer().send(response);
        user.save();

        if(resultStars >= 3) {

            ResponseUnionCommanderCardBroPacket broadcast = CommanderService.getInstance().getUnionCommanderCardBroPacket(user, mergeData.getCommanderData().getSkillId(), resultStars, failed);
            PacketRouter.getInstance().broadcast(broadcast);

        }
        return;

    }

    @PacketProcessor
    public void onCommanderUnionStone(RequestCommanderUnionStonePacket packet) throws BadGuidException {

        LoginService.validate(packet, packet.getGuid());
        User user = PacketService.getUserRepository().findByGuid(packet.getGuid());

        if(user == null)
            return;

        Prop gem = user.getInventory().getProp(packet.getPropsId());

        if(gem == null)
            return;

        PropData data = gem.getData();

        if(!data.getType().equals("gem"))
            return;

        PropGemData gemData = data.getGemData();

        if(gemData.getLevel() >= 4)
            return;

        boolean lock = packet.getLockFlag() != 0;

        if(!user.getInventory().removeProp(gem, 4, lock))
            return;

        int result = gem.getPropId() + 1;

        if(gem.getPropId() == 1119) {

            List<PropData> gems = ResourceManager.getProps().getGems();
            List<PropData> raws = gems.stream().filter(g -> g.getGemData().getType() >= 1 && g.getGemData().getType() <= 11 && g.getGemData().getLevel() == 0).collect(Collectors.toList());

            Collections.shuffle(raws);
            result = raws.get(0).getId();

        }

        if(!user.getInventory().addProp(result, 1, 0, lock))
            return;

        boolean bro = gemData.getLevel() >= 2;

        ResponseCommanderUnionStonePacket response = CommanderService.getInstance().getCommanderUnionStonePacket(user, result, lock, bro);

        packet.getSmartServer().send(response);
        user.save();

        if(bro)
            PacketRouter.getInstance().broadcast(response, user);

        return;

    }

    @PacketProcessor
    public void onCommanderPropertyStone(RequestCommanderPropertyStonePacket packet) throws BadGuidException {

        LoginService.validate(packet, packet.getGuid());
        User user = PacketService.getUserRepository().findByGuid(packet.getGuid());

        if(user == null)
            return;

        PropData objGemData = ResourceManager.getProps().getGemData(packet.getObjStoneId());

        PropData srcData1 = ResourceManager.getProps().getGemData(packet.getSrcStoneId1());
        PropData srcData2 = ResourceManager.getProps().getGemData(packet.getSrcStoneId2());
        PropData srcData3 = ResourceManager.getProps().getGemData(packet.getSrcStoneId3());

        if(objGemData == null || srcData1 == null || srcData2 == null)
            return;

        if(!objGemData.getType().equals("gem") || !srcData1.getType().equals("gem") || !srcData2.getType().equals("gem"))
            return;

        if(srcData3 != null && !srcData3.getType().equals("gem"))
            return;

        Gem obj = Gem.of(objGemData.getGemData());

        Gem src1 = Gem.of(srcData1.getGemData());
        Gem src2 = Gem.of(srcData2.getGemData());
        Gem src3 = srcData3 == null ? null : Gem.of(srcData3.getGemData());

        boolean locked = packet.getLockFlag() == 1;
        boolean validation = CommanderService.getInstance().validateGemEX(obj, src1, src2, src3);

        if(!validation)
            return;

        if(!user.getInventory().removeProp(srcData1.getId(), 1, 0, locked))
            return;

        if(!user.getInventory().removeProp(srcData2.getId(), 1, 0, locked))
            return;

        if(srcData3 != null)
            if(!user.getInventory().removeProp(srcData3.getId(), 1, 0, locked))
                return;

        if(!user.getInventory().addProp(objGemData.getId(), 1, 0, locked))
            return;

        if(user.getResources().getGold() < 10000)
            return;

        user.getResources().setGold(user.getResources().getGold() - 10000);

        boolean bro = obj.getLevel() >= 3;

        ResponseCommanderPropertyStonePacket response = CommanderService.getInstance().getCommanderPropertyStonePacket(user, packet.getType(), packet.getObjStoneId(), packet.getSrcStoneId1(), packet.getSrcStoneId2(), packet.getSrcStoneId3(), locked, bro);

        packet.getSmartServer().send(response);
        user.save();

        if(bro)
            PacketRouter.getInstance().broadcast(response, user);

        return;

    }

    @PacketProcessor
    public void onCommanderSeal(RequestCommanderChangeCardPacket packet) throws BadGuidException {

        LoginService.validate(packet, packet.getGuid());
        User user = PacketService.getUserRepository().findByGuid(packet.getGuid());

        if(user == null)
            return;

        Prop card = user.getInventory().getProp(926);

        if(card == null)
            return;

        Commander commander = user.getCommander(packet.getCommanderId());

        if(commander == null || commander.getChips().size() > 0 || commander.hasGems() || commander.isCommon())
            return;

        if(!user.getInventory().removeSmartProp(card, 1))
            return;

        ResponseCommanderChangeCardPacket response = CommanderService.getInstance().getSealCommanderPacket(user, card, commander);
        user.save();

        packet.getSmartServer().send(response);

    }

    @PacketProcessor
    public void onRelive(RequestResumeCommanderPacket packet) throws BadGuidException {

        LoginService.validate(packet, packet.getGuid());
        User user = PacketService.getUserRepository().findByGuid(packet.getGuid());

        if(user == null)
            return;

        Prop card = user.getInventory().getProp(904);

        if(card == null)
            return;

        Commander commander = user.getCommander(packet.getCommanderId());

        if(commander == null || commander.getState() != 1)
            return;

        if(!user.getInventory().removeSmartProp(card, 1))
            return;

        ResponseResumeCommanderPacket response = CommanderService.getInstance().getResumeCommanderPacket(card, commander);
        user.save();

        packet.getSmartServer().send(response);

    }

    @PacketProcessor
    public void onRelive(RequestReliveCommanderPacket packet) throws BadGuidException {

        LoginService.validate(packet, packet.getGuid());
        User user = PacketService.getUserRepository().findByGuid(packet.getGuid());

        if(user == null)
            return;

        Prop card = user.getInventory().getProp(903);

        if(card == null)
            return;

        Commander commander = user.getCommander(packet.getCommanderId());

        if(commander == null || commander.getState() != 2)
            return;

        if(!user.getInventory().removeSmartProp(card, 1))
            return;

        ResponseReliveCommanderPacket response = CommanderService.getInstance().getReliveCommanderPacket(card, commander);
        user.save();

        packet.getSmartServer().send(response);

    }

    @PacketProcessor
    public void onReset(RequestClearCommanderPercentPacket packet) throws BadGuidException {

        LoginService.validate(packet, packet.getGuid());
        User user = PacketService.getUserRepository().findByGuid(packet.getGuid());

        if(user == null)
            return;

        Prop card = user.getInventory().getProp(924);

        System.out.println(card);

        if(card == null)
            return;

        Commander commander = user.getCommander(packet.getCommanderId());

        if(commander == null || commander.hasChips() || commander.hasGems() || commander.isCommon())
            return;

        if(!user.getInventory().removeSmartProp(card, 1))
            return;

        ResponseClearCommanderPercentPacket response = CommanderService.getInstance().getResetCommanderPacket(card, commander);
        user.save();

        packet.getSmartServer().send(response);

    }

    @PacketProcessor
    public void onCommanderStoneInfo(RequestCommanderStoneInfoPacket packet) {

        Commander commander = CommanderService.getInstance().getCommander(packet.getCommanderId());

        if(commander == null)
            return;

        packet.getSmartServer().send(CommanderService.getInstance().getCommanderStoneInfo(commander));

    }

    @PacketProcessor
    public void onDeleteCommander(RequestDeleteCommanderPacket packet) throws BadGuidException {

        LoginService.validate(packet, packet.getGuid());
        Commander commander = CommanderService.getInstance().getCommander(packet.getCommanderId());

        if(commander == null)
            return;

        ResponseDeleteCommanderPacket response = CommanderService.getInstance().getDeleteCommanderPacket(commander);

        if(response == null)
            return;

        packet.getSmartServer().send(response);

    }

}