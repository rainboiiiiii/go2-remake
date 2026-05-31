package com.go2super.obj.type;

import com.go2super.database.entity.Commander;
import com.go2super.database.entity.GameBoost;
import com.go2super.database.entity.sub.UserBoost;
import com.go2super.obj.utility.PropConsumption;
import com.go2super.packet.props.ResponseUsePropsPacket;
import com.go2super.resources.data.PropData;
import com.go2super.resources.data.props.PropCommanderData;
import com.go2super.service.CommanderService;
import com.go2super.service.ResourcesService;
import com.go2super.service.UserService;
import lombok.Getter;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.IntStream;

@Getter
public enum PropAction {

    BASIC_BOOSTERS(array(905, 906, 907, 900, 930, 943), ((prop, quantity, lock, packet, user) -> {

        GameBoost boost = ResourcesService.getInstance().getBoostRepository().findByPropId(prop.getPropId());

        if(boost == null)
            throw new IllegalArgumentException("Basic booster " + prop.getPropId() + " not found!");

        user.getStats().addBonusTime(boost, quantity * boost.getSeconds());

        ResponseUsePropsPacket response = ResourcesService.getInstance().genericUseProps(boost.getPropId(), quantity, lock ? 1 : 0, 1);
        UserService.getInstance().getUserRepository().save(user);

        packet.getSmartServer().send(response);
        packet.getSmartServer().send(user.getQueuesAsPacket());

    })),

    PLANET_BOOSTERS(array(979, 4458, 4513, 939, 940, 941, 942), ((prop, quantity, lock, packet, user) -> {

        GameBoost boost = ResourcesService.getInstance().getBoostRepository().findByPropId(prop.getPropId());
        user.update();

        if(boost == null)
            throw new IllegalArgumentException("Planet booster " + prop.getPropId() + " not found!");

        List<UserBoost> toRemove = new ArrayList<>();

        for(UserBoost userBoost : user.getStats().getBuffs())
            if(!userBoost.getGameBoostId().equals(boost.getId().toString()))
                for(BonusType bonusType : userBoost.boost().getBonuses())
                    if(bonusType == BonusType.PLANET_APPEARANCE)
                        toRemove.add(userBoost);

        user.getStats().getBuffs().removeAll(toRemove);
        user.getStats().addBonusTime(boost, quantity * boost.getSeconds());

        ResponseUsePropsPacket response = ResourcesService.getInstance().genericUseProps(boost.getPropId(), quantity, lock ? 1 : 0, 1);
        UserService.getInstance().getUserRepository().save(user);

        packet.getSmartServer().send(response);
        packet.getSmartServer().send(user.getQueuesAsPacket());

    })),

    TRUCE_BOOSTERS(array(902, 937), ((prop, quantity, lock, packet, user) -> {

        if(quantity > 1)
            return;

        GameBoost boost = ResourcesService.getInstance().getBoostRepository().findByPropId(prop.getPropId());
        user.update();

        if(boost == null)
            throw new IllegalArgumentException("Planet truce booster " + prop.getPropId() + " not found!");

        List<BonusType> bonuses = user.getStats().getAllBonuses();

        if(bonuses.contains(BonusType.TRUCE_IMPEDIMENT) || bonuses.contains(BonusType.PLANET_PROTECTION))
            return;

        if(prop.getPropId() == 902) {

            GameBoost impediment = ResourcesService.getInstance().getBoostRepository().findByMimeType(6);
            user.getStats().addBonusTime(impediment, impediment.getSeconds());

        }

        user.getStats().addBonusTime(boost, boost.getSeconds());

        ResponseUsePropsPacket response = ResourcesService.getInstance().genericUseProps(boost.getPropId(), quantity, lock ? 1 : 0, 1);
        UserService.getInstance().getUserRepository().save(user);

        packet.getSmartServer().send(response);
        packet.getSmartServer().send(user.getQueuesAsPacket());

    })),

    COMMANDERS_CARDS(matrix(range(0, 512), range(2007, 2501)), ((prop, quantity, lock, packet, user) -> {

        if(quantity > 1)
            return;

        PropData propData = CommanderService.getInstance().getCommanderPropData(prop.getPropId());

        if(propData == null)
            return;

        PropCommanderData commanderData = propData.getCommanderData();
        Commander commander = user.getCommanderBySkill(commanderData.getSkillId());

        if(commander != null)
            return;

        int stars = prop.getPropId() - propData.getId();

        commander = CommanderService.getInstance().basic(commanderData.getSkillId(), stars, 0, user.getPlanet().getUserId());
        commander.save();

        ResponseUsePropsPacket response = ResourcesService.getInstance().genericUseProps(prop.getPropId(), quantity, lock ? 1 : 0, 1);
        UserService.getInstance().getUserRepository().save(user);

        packet.getSmartServer().send(CommanderService.getInstance().getCreateCommander(commander));
        packet.getSmartServer().send(response);

    })),


    ;

    private LinkedList<Integer> listen = new LinkedList<>();

    private PropConsumption action;

    PropAction(int propId, PropConsumption consumption) {
        this.listen.add(propId);
        this.action = consumption;
    }

    PropAction(int start, int end, PropConsumption consumption) {
        for(int i = start; i < end; i++)
            listen.add(i);
        this.action = consumption;
    }

    PropAction(int[][] ranges, PropConsumption consumption) {
        for(int i = 0; i < ranges.length; i++)
            for(int j = 0; j < ranges[i].length; j++)
                listen.add(ranges[i][j]);
        this.action = consumption;
    }

    PropAction(int[] some, PropConsumption consumption) {
        for(int i : some)
            listen.add(i);
        this.action = consumption;
    }

    public static PropAction getAction(int propId) {
        for(PropAction type : values())
            if(type.getListen().contains(propId))
                return type;
        return null;
    }

    public static int[][] matrix(int[]...some) {
        return some;
    }

    public static int[] array(int...some) {
        return some;
    }

    public static int[] range(int from, int to) {
        return IntStream.rangeClosed(from, to).toArray();
    }

}
