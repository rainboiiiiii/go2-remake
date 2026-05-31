package com.go2super.service;

import com.go2super.database.entity.Commander;
import com.go2super.database.entity.Fleet;
import com.go2super.database.entity.User;
import com.go2super.database.entity.sub.CommanderExpertise;
import com.go2super.database.repository.CommanderRepository;
import com.go2super.obj.game.*;
import com.go2super.obj.utility.Gem;
import com.go2super.packet.commander.*;
import com.go2super.packet.gems.ResponseCommanderInsertStonePacket;
import com.go2super.packet.gems.ResponseCommanderPropertyStonePacket;
import com.go2super.packet.gems.ResponseCommanderUnionStonePacket;
import com.go2super.resources.ResourceManager;
import com.go2super.resources.data.CommanderLevelsData;
import com.go2super.resources.data.CommanderPullulateData;
import com.go2super.resources.data.CommanderStatsData;
import com.go2super.resources.data.PropData;
import com.go2super.socket.util.ListUtil;
import com.go2super.socket.util.MathUtil;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Getter
@Service
public class CommanderService {

    private static final String COMMON_EXPERTISE_PATTERN = "ABBBBBBCCCCCCCDDDDDDD";
    private static final int MAXIMUM_EXPERIENCE = 3748500;

    private static CommanderService instance;

    @Autowired
    CommanderRepository commanderRepository;

    public CommanderService() {

        instance = this;

    }

    public ResponseUnionCommanderCardBroPacket getUnionCommanderCardBroPacket(User user, int skillId, int stars, boolean failed) {

        ResponseUnionCommanderCardBroPacket response = new ResponseUnionCommanderCardBroPacket();

        response.setGuid(user.getGuid());
        response.setUserId(user.getPlanet().getUserId());
        response.setSkillId(skillId);
        response.setCardLevel(stars);
        response.setSuccessFlag(failed ? 0 : 1);
        response.getName().value(user.getUsername());

        return response;

    }

    public ResponseUnionCommanderCardPacket getUnionCommanderCardPacket(int winCC, int card1, int card2, int card3, int goods, int goodsFlag) {

        ResponseUnionCommanderCardPacket response = new ResponseUnionCommanderCardPacket();

        response.setPropsId(winCC);

        response.setCard1(card1);
        response.setCard2(card2);
        response.setCard3(card3);

        response.setGoods(goods);
        response.setGoodsLockFlag(goodsFlag);
        return response;

    }

    public ResponseCommanderChangeCardPacket getSealCommanderPacket(User user, Prop sealCard, Commander commander) {

        ResponseCommanderChangeCardPacket response = new ResponseCommanderChangeCardPacket();

        user.getInventory().addProp(commander.propId(), 1, 0, false);

        response.setCommanderId(commander.getCommanderId());
        response.setPropsId(commander.propId());
        response.setLockFlag(0);

        response.setUsePropsId(sealCard.getPropId());
        response.setUseLockFlag(sealCard.getPropLockNum());
        commander.delete();

        return response;

    }

    public ResponseCommanderUnionStonePacket getCommanderUnionStonePacket(User user, int result, boolean lock, boolean bro) {

        ResponseCommanderUnionStonePacket response = new ResponseCommanderUnionStonePacket();

        response.setGuid(user.getGuid());
        response.getName().value(user.getUsername());
        response.setBroFlag(bro ? 1 : 0);

        response.setLockFlag(lock ? 1 : 0);
        response.setPropsId(result);

        return response;

    }

    public ResponseCommanderPropertyStonePacket getCommanderPropertyStonePacket(User user, int type, int obj, int src1, int src2, int src3, boolean lock, boolean bro) {

        ResponseCommanderPropertyStonePacket response = new ResponseCommanderPropertyStonePacket();

        response.setGuid(user.getGuid());
        response.getName().value(user.getUsername());
        response.setBroFlag(bro ? 1 : 0);
        response.setType(type);

        response.setLockFlag(lock ? 1 : 0);
        response.setObjStoneId(obj);
        response.setSrcStoneId1(src1);
        response.setSrcStoneId2(src2);
        response.setSrcStoneId3(src3);

        return response;

    }

    public ResponseCommanderInsertStonePacket getCommanderInsertStonePacket(int gemType, int commanderId, int holeId, int propsId, int lockFlag) {

        ResponseCommanderInsertStonePacket response = new ResponseCommanderInsertStonePacket();

        response.setGemType(gemType);
        response.setCommanderId(commanderId);
        response.setHoleId(holeId);
        response.setPropsId(propsId);
        response.setLockFlag(lockFlag);

        return response;

    }

    public ResponseResumeCommanderPacket getResumeCommanderPacket(Prop revivalCard, Commander commander) {

        ResponseResumeCommanderPacket response = new ResponseResumeCommanderPacket();

        commander.setState(0);
        commander.save();

        response.setCommanderId(commander.getCommanderId());
        response.setPropsId(revivalCard.getPropId());
        response.setLockFlag(revivalCard.getPropLockNum());

        return response;

    }

    public ResponseReliveCommanderPacket getReliveCommanderPacket(Prop revivalCard, Commander commander) {

        ResponseReliveCommanderPacket response = new ResponseReliveCommanderPacket();

        commander.setState(0);
        commander.save();

        response.setCommanderId(commander.getCommanderId());
        response.setPropsId(revivalCard.getPropId());
        response.setLockFlag(revivalCard.getPropLockNum());

        return response;

    }

    public ResponseClearCommanderPercentPacket getResetCommanderPacket(Prop resetCard, Commander commander) {

        ResponseClearCommanderPercentPacket response = new ResponseClearCommanderPercentPacket();

        commander.reset();
        commander.save();

        CommanderLevel level = commander.getLevel();
        CommanderAttributes attributes = commander.getAttributes();

        response.setCommanderId(commander.getCommanderId());
        response.setLevel(level.getLevel());
        response.setExp(level.getLevelExperience());

        response.setAim((short) attributes.getAim());
        response.setBlench((short) attributes.getDodge());
        response.setPriority((short) attributes.getSpeed());
        response.setElectron((short) attributes.getElectron());

        response.setLockFlag(resetCard.getPropLockNum());

        response.setAimPer((char) commander.getGrowthAim());
        response.setBlenchPer((char) commander.getGrowthDodge());
        response.setPriorityPer((char) commander.getGrowthSpeed());
        response.setElectronPer((char) commander.getGrowthElectron());

        return response;

    }

    public ResponseDeleteCommanderPacket getDeleteCommanderPacket(Commander commander) {

        commanderRepository.delete(commander);

        ResponseDeleteCommanderPacket packet = new ResponseDeleteCommanderPacket();

        packet.setCommanderId(commander.getCommanderId());

        return packet;

    }

    public ResponseCommanderBaseInfoPacket getBaseInfoPacket(User user) {

        ResponseCommanderBaseInfoPacket packet = new ResponseCommanderBaseInfoPacket();

        packet.setNextInviteTime(user.getNextRecruit());
        packet.setReserve(0);

        for(Commander commander : user.getCommanders())
            packet.getCommanderBaseInfos().add(getCommanderBaseInfo(commander));

        packet.setDataLen(packet.getCommanderBaseInfos().size());

        return packet;

    }

    public ResponseCommanderInfoPacket getInfoPacket(int commanderId, int showType, User user) {

        List<Commander> commanders = user.getCommanders();

        for(Commander commander : commanders)
            if(commander.getCommanderId() == commanderId) {

                List<Commander> statuses = new ArrayList<>(commanders);
                statuses.remove(commander);

                ResponseCommanderInfoPacket response = new ResponseCommanderInfoPacket();
                CommanderLevel level = getLevel(commander);
                CommanderAttributes attributes = getAttributes(commander);

                Fleet fleet = commander.getFleet();

                response.setLevel((char) level.getLevel());
                response.setCommanderId(commanderId);
                response.setShipTeamId(fleet == null ? -1 : fleet.getShipTeamId());
                response.setCardType((char) commander.getType());
                response.setSkill((short) commander.getSkill());
                response.setCardLevel((short) commander.getStars());
                response.setShowType((char) showType);
                response.setState((char) commander.getState());

                if(fleet != null) {

                    response.setTarget((char) fleet.getTarget());
                    response.setTargetInterval((char) fleet.getTargetInterval());

                    response.setTeamBody(fleet.getFleetBody().getCells());

                }

                response.setAim((short) attributes.getAim());
                response.setElectron((short) attributes.getElectron());
                response.setBlench((short) attributes.getDodge());
                response.setPriority((short) attributes.getSpeed());

                response.setAimPer((char) commander.getGrowthAim());
                response.setElectronPer((char) commander.getGrowthElectron());
                response.setBlenchPer((char) commander.getGrowthDodge());
                response.setPriorityPer((char) commander.getGrowthSpeed());

                response.setExp(level.getLevelExperience());
                response.setStone(new ShortArray(ListUtil.toArray(commander.getGems())));
                response.setStoneHole(level.getLevelData().getGem());

                for(Commander status : statuses)
                    response.getAllStatus().add(getCommanderInfo(status));

                response.setAllStatusLen((char) response.getAllStatus().size());

                response.getCommanderZJ().value(commander.getExpertise().getJZ());
                return response;

            }

        return null;

    }

    public ResponseCommanderStoneInfoPacket getCommanderStoneInfo(Commander commander) {

        ResponseCommanderStoneInfoPacket response = new ResponseCommanderStoneInfoPacket();

        CommanderLevel level = getLevel(commander);
        CommanderAttributes attributes = getAttributes(commander);

        response.getUserName().value(commander.getUser().getUsername());
        response.getCommanderZJ().value(commander.getExpertise().getJZ());

        response.setExp(level.getLevelExperience());
        response.setSkillId(commander.getSkill());
        response.setLevel(level.getLevel());
        response.setCardLevel(commander.getStars());

        response.setAim(attributes.getAim());
        response.setBlench(attributes.getDodge());
        response.setPriority(attributes.getSpeed());
        response.setElectron(attributes.getElectron());

        response.setStoneAim(commander.getStoneAim());
        response.setStoneBlench(commander.getStoneDodge());
        response.setStoneElectron(commander.getStoneElectron());
        response.setStonePriority(commander.getStonePriority());

        response.setStoneAssault(commander.getStoneAssault());
        response.setStoneEndure(commander.getStoneEndure());
        response.setStoneShield(commander.getStoneShield());
        response.setStoneBlastHurt(commander.getStoneBlastHurt());
        response.setStoneBlast(commander.getStoneBlastHurt());
        response.setStoneDoubleHit(commander.getStoneDoubleHit());
        response.setStoneRepairShield(commander.getStoneRepairShield());
        response.setStoneExp(commander.getStoneExp());

        response.setAimPer((char) commander.getGrowthAim());
        response.setBlenchPer((char) commander.getGrowthDodge());
        response.setPriorityPer((char) commander.getGrowthSpeed());
        response.setElectronPer((char) commander.getGrowthElectron());

        return response;

    }

    public ResponseCreateCommanderPacket getCreateCommander(Commander commander) {

        ResponseCreateCommanderPacket response = new ResponseCreateCommanderPacket();

        response.setNextInviteTime(commander.getUser().getNextRecruit());
        response.setCommanderBaseInfo(getCommanderBaseInfo(commander));

        return response;

    }

    public CommanderBaseInfo getCommanderBaseInfo(Commander commander) {

        CommanderBaseInfo baseInfo = new CommanderBaseInfo();
        CommanderLevel level = getLevel(commander);

        baseInfo.getName().value(commander.getName());
        baseInfo.setUserId(commander.getUserId());
        baseInfo.setCommanderId(commander.getCommanderId());
        baseInfo.setShipTeamId(-1);
        baseInfo.setState(commander.getState());
        baseInfo.setSkill((short) commander.getSkill());
        baseInfo.setLevel((char) level.getLevel());
        baseInfo.setType((char) commander.getType());

        return baseInfo;

    }

    public CommanderInfo getCommanderInfo(Commander commander) {

        CommanderInfo info = new CommanderInfo();

        info.setCommanderId(commander.getCommanderId());
        info.setState(commander.getState());

        return info;

    }

    public CommanderStatsData getStats(Commander commander) {
        return getStats(commander.getSkill());
    }

    public CommanderStatsData getStats(int skillId) {
        if(skillId == -1)
            return null;
        return ResourceManager.getCommanders().getStats().get(skillId);
    }

    public CommanderLevel getLevel(Commander commander) {
        return getLevel(commander.getExperience());
    }

    public CommanderLevel getLevel(int experience) {

        List<CommanderLevelsData> levels = ResourceManager.getCommanders().getLevels();
        int level = 0;
        int total = 0;

        if(experience >= MAXIMUM_EXPERIENCE)
            return new CommanderLevel(levels.get(levels.size() - 1), 49, 0);

        for(CommanderLevelsData data : levels) {

            level++;
            total += data.getExp();

            if(experience < total) {
                int real = (data.getExp() + experience) - total;
                return new CommanderLevel(data, level - 1, real);
            }

        }

        return new CommanderLevel(ResourceManager.getCommanders().getLevels().get(0), level, 0);

    }

    public void randomizeVariance(Commander commander) {
        commander.setVariance(MathUtil.random(-30, 32));
    }

    public void randomizeGrowth(Commander commander) {

        int aim = MathUtil.random(0, 51);
        int electron = MathUtil.random(0, 51);

        int dodge = 0;
        int speed = 0;

        if(MathUtil.random()) {

            dodge = 50 - aim;
            speed = 50 - electron;

        } else {

            dodge = 50 - electron;
            speed = 50 - aim;

        }

        commander.setGrowthAim(aim);
        commander.setGrowthElectron(electron);
        commander.setGrowthDodge(dodge);
        commander.setGrowthSpeed(speed);

    }

    public Commander basic(int skill, long userId) {
        return basic(skill, 0, 0, userId);
    }

    public Commander basic(int skill, int stars, int experience, long userId) {

        CommanderStatsData statsData = CommanderService.getInstance().getStats(skill);

        if(statsData == null)
            return common(userId);

        Commander commander = new Commander();

        commander.setName(statsData.getName());
        commander.setCommanderId(getNextCommanderId());
        commander.setUserId(userId);
        commander.setSkill(skill);
        commander.setStars(stars);
        commander.setExperience(experience);

        commander.randomizeGrowth();
        commander.randomizeVariance();

        commander.setGems(Arrays.asList(-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1));

        return commander;

    }

    public Commander common(long userId) {

        Commander commander = new Commander();

        commander.setName("Commander");
        commander.setCommanderId(getNextCommanderId());
        commander.setUserId(userId);
        commander.setSkill(-1);
        commander.setStars(-1);
        commander.setExperience(0);

        commander.setCommonBaseAim(MathUtil.random(0, 11));
        commander.setCommonBaseElectron(MathUtil.random(0, 11));
        commander.setCommonBaseDodge(MathUtil.random(0, 11));
        commander.setCommonBaseSpeed(MathUtil.random(0, 11));
        commander.setCommonExpertise(CommanderExpertise.common());

        commander.randomizeGrowth();
        commander.randomizeVariance();

        commander.setGems(Arrays.asList(-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1));

        return commander;

    }

    public CommanderAttributes getAttributes(Commander commander) {

        int stars = commander.isCommon() ? 0 : commander.getStars();

        int aim = (int) (getGrowth(commander, commander.getGrowthAim()) + commander.getBaseAim() + stars);
        int electron = (int) (getGrowth(commander, commander.getGrowthElectron()) + commander.getBaseElectron() + stars);
        int dodge = (int) (getGrowth(commander, commander.getGrowthDodge()) + commander.getBaseDodge() + stars);
        int speed = (int) (getGrowth(commander, commander.getGrowthSpeed()) + commander.getBaseSpeed() + stars);

        return new CommanderAttributes(aim, electron, dodge, speed);

    }

    public double getGrowth(Commander commander, double rate) {

        CommanderPullulateData pullulate = getCommanderPullulate(commander);
        double gain = getGain(pullulate);

        if(commander.isTitan())
            gain += 5;

        double variance = (commander.getVariance() - 0.025) / 200;
        return Math.floor(rate / 100 * (gain + 0.5 + variance) * (double) getLevel(commander).getLevel());

    }

    public int getGain(CommanderPullulateData pullulate) {
        return pullulate.getMinPullulate();
    }

    public int getPropId(Commander commander) {

        List<PropData> props = ResourceManager.getProps().getCommanders();

        for(PropData data : props)
            if(data.hasCommanderData())
                if (data.getCommanderData().getSkillId() == commander.getSkill())
                    return data.getId() + commander.getStars();

        return -1;

    }

    public PropData getCommanderPropData(int propId) {

        List<PropData> props = ResourceManager.getProps().getCommanders();

        for(PropData data : props)
            if(data.hasCommanderData())
                if ((data.getId() + 8) >= propId && data.getId() <= propId)
                    return data;

        return null;

    }

    public CommanderPullulateData getCommanderPullulate(Commander commander) {
        return getCommanderPullulate(commander.isCommon() ? 0 : commander.getStats().getType(), commander.getStars());
    }

    public CommanderPullulateData getCommanderPullulate(int commanderType, int stars) {

        for(CommanderPullulateData data : getPullulateData())
            if(data.getCommandType() == commanderType && data.getCommandStar() == stars)
                return data;

        return getPullulateData().get(0);

    }

    public List<CommanderPullulateData> getPullulateData() {
        return ResourceManager.getCommanders().getPullulates();
    }

    public Commander getCommander(int commanderId) {
        return commanderRepository.findByCommanderId(commanderId);
    }

    public List<Commander> getCommanders(User user) {

        List<Commander> commanders = commanderRepository.findByUserId(user.getPlanet().getUserId());

        if(commanders == null)
            return new ArrayList<>();

        return commanders;

    }

    public boolean validateGemEX(Gem objGem, Gem srcGem1, Gem srcGem2, Gem srcGem3) {

        if(srcGem1 != null && srcGem2 != null && srcGem3 != null) {
            //triad

            //level must match
            if(srcGem1.level != srcGem2.level || srcGem2.level != srcGem3.level || srcGem3.level != objGem.level)
                return false;

            int add = srcGem1.type + srcGem2.type + srcGem3.type;
            int mul = srcGem1.type * srcGem2.type * srcGem3.type;

            //wtf
            switch(objGem.type) {
                case 21:
                    return add == (5 + 6 + 10) && mul == (5 * 6 * 10);
                case 22:
                    return add == (6 + 7 + 11) && mul == (6 * 7 * 11);
                case 23:
                    return add == (8 + 9 + 10) && mul == (8 * 9 * 10);
                default:
                    return false;
            }

        } else if(srcGem1 != null && srcGem2 != null) {
            //quadra

            //color must match
            if(srcGem1.color != srcGem2.color || srcGem2.color != objGem.color)
                return false;
            //level must equal the average level of the two gems, rounded down
            if((srcGem1.level + srcGem2.level) / 2 != objGem.level)
                return false;

            //order gems lowest to highest
            if(srcGem1.type > srcGem2.type) {
                Gem srcGemSwap = srcGem2;
                srcGem2 = srcGem1;
                srcGem1 = srcGemSwap;
            }

            //lower gem must be one of the basic stat gems
            if(srcGem1.type < 1 || srcGem1.type > 4)
                return false;
            //higher gem must be a triad
            if(srcGem2.type < 21 || srcGem2.type > 23)
                return false;

            return 23 + srcGem1.type == objGem.type;

        }

        return false;

    }

    public int getNextCommanderId() {

        Commander last = commanderRepository.findTopByOrderByIdDesc();

        if(last == null)
            return 1;

        return last.getCommanderId() + 1;

    }

    public String getCommonExpertisePattern() {
        return COMMON_EXPERTISE_PATTERN;
    }

    public int getMaximiumExperience() {
        return MAXIMUM_EXPERIENCE;
    }

    public static CommanderService getInstance() {
        return instance;
    }

}
