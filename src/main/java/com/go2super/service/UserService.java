package com.go2super.service;

import com.go2super.database.entity.GameBoost;
import com.go2super.database.entity.User;
import com.go2super.database.entity.sub.UserBoost;
import com.go2super.database.entity.sub.UserBuilding;
import com.go2super.database.entity.sub.UserPlanet;
import com.go2super.database.repository.GameBoostRepository;
import com.go2super.database.repository.UserRepository;
import com.go2super.obj.game.BuildInfo;
import com.go2super.obj.game.TimeQueue;
import com.go2super.obj.type.BonusType;
import com.go2super.obj.utility.GalaxyRegion;
import com.go2super.obj.utility.GameCell;
import com.go2super.obj.utility.GameRegion;
import com.go2super.packet.Packet;
import com.go2super.packet.mall.ResponseBuyGoodsPacket;
import com.go2super.packet.props.ResponseTimeQueuePacket;
import com.go2super.resources.data.PropData;
import com.go2super.resources.data.props.PropMallData;
import com.go2super.socket.util.DateUtil;
import com.go2super.socket.util.MathUtil;
import lombok.Getter;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.swing.plaf.synth.SynthOptionPaneUI;
import java.sql.Time;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class UserService {

    private static UserService userService;

    @Autowired
    @Getter
    private UserRepository userRepository;

    @Autowired
    @Getter
    private GameBoostRepository boostRepository;

    public UserService() {
        userService = this;
    }

    public ResponseTimeQueuePacket getUserQueues(User user) {

        ResponseTimeQueuePacket timeQueuePacket = new ResponseTimeQueuePacket();
        LinkedList<TimeQueue> queues = new LinkedList<>();

        List<Pair<UserBoost, GameBoost>> deBuffs = new ArrayList<>();

        for(UserBoost userBoost : user.getStats().getBuffs()) {

            if(queues.size() >= 10)
                break;

            if(userBoost.getUntil().getTime() < DateUtil.now().getTime())
                continue;

            Optional<GameBoost> optional = boostRepository.findById(userBoost.getGameBoostId());

            if(optional.isPresent()) {

                GameBoost boost = optional.get();

                if(boost.getPropId() == -1) {
                    deBuffs.add(Pair.of(userBoost, boost));
                    continue;
                }

                if(boost.getSeconds() > 0)
                    queues.add(new TimeQueue(boost.getMimeType(), userBoost.getSeconds()));

            }

        }

        for(Pair<UserBoost, GameBoost> deBuff : deBuffs)
            if(queues.size() < 10)
                queues.addFirst(new TimeQueue(deBuff.getValue().getMimeType(), deBuff.getKey().getSeconds()));

        timeQueuePacket.setDataLen(queues.size());
        timeQueuePacket.setTimeQueues(queues);

        while(timeQueuePacket.getTimeQueues().size() < 10)
            timeQueuePacket.getTimeQueues().add(TimeQueue.generate());


        return timeQueuePacket;

    }

    public void updateStats(User user) {

        Date lastCalculus = user.getStorage().getLastProductionCalculus();
        int seconds = DateUtil.seconds(lastCalculus).intValue();

        List<UserBoost> expiredBoosts = getExpiredBoosts(user);

        for(UserBoost expired : expiredBoosts) {

            updateStorage(user, lastCalculus, expired.getUntil());

            calculateProductivity(user);
            lastCalculus = expired.getUntil();
            user.getStats().removeBoost(expired);

        }

        Date now = DateUtil.now();
        updateStorage(user, lastCalculus, now);

        calculateProductivity(user);
        user.getStorage().setLastProductionCalculus(now);
        userRepository.save(user);

    }

    public void updateStorage(User user, Date from, Date to) {

        double goldProduction = user.getStorage().getGoldProduction();
        double he3Production = user.getStorage().getHe3Production();
        double metalProduction = user.getStorage().getMetalProduction();

        double time = Long.valueOf(TimeUnit.MILLISECONDS.toSeconds(to.getTime() - from.getTime())).intValue();

        int totalGoldProduction = (int) ((goldProduction / 3600) * time);
        int totalHe3Production = (int) ((he3Production / 3600)  * time);
        int totalMetalProduction = (int) ((metalProduction / 3600)  * time);

        user.getStorage().addGold(totalGoldProduction);
        user.getStorage().addHe3(totalHe3Production);
        user.getStorage().addMetal(totalMetalProduction);

    }

    public List<UserBoost> getExpiredBoosts(User user) {

        if(user.getStats().getBuffs() == null)
            user.getStats().setBuffs(new ArrayList<>());

        Date now = DateUtil.now();
        List<UserBoost> boosts = user.getStats().getBuffs().stream().filter(boost -> boost.getUntil().before(now)).collect(Collectors.toList());

        Collections.sort(boosts, (UserBoost ub1, UserBoost ub2) -> ub1.getUntil().after(ub2.getUntil()) ? 1 : 0);
        return boosts;

    }

    public List<BuildInfo> getBuilds(User user) {

        List<BuildInfo> buildInfos = new ArrayList<>();
        boolean save = false;

        for(UserBuilding userBuilding : user.getBuildings().getBuildings()) {

            Long spareTime = Long.valueOf(0);

            Long repairingTime = userBuilding.repairingTime();
            Long updatingTime = userBuilding.updatingTime();

            if(userBuilding.getRepairing() != null)
                if(userBuilding.getRepairing() && repairingTime <= 0) {

                    save = true;

                    userBuilding.setRepairing(false);
                    userBuilding.setUntilRepair(null);

                } else {

                    spareTime = repairingTime;

                }

            if(userBuilding.getUpdating() != null)
                if(userBuilding.getUpdating() && updatingTime <= 0) {

                    save = true;

                    userBuilding.setLevelId(userBuilding.getLevelId() + 1);
                    userBuilding.setUpdating(false);
                    userBuilding.setUntilUpdate(null);

                } else if(userBuilding.getUpdating()){

                    spareTime = updatingTime;

                }

            buildInfos.add(userBuilding.getInfo(spareTime.intValue(), buildInfos.size()));

        }

        if(save)
            user.save();

        return buildInfos;

    }

    public void calculateProductivity(User user) {

        int gainGold = user.getBuildings().getGoldGain();
        int gainHe3 = user.getBuildings().getHe3Gain();
        int gainMetal = user.getBuildings().getMetalGain();

        double goldBonus = user.getBuildings().getGoldBonus();
        double he3Bonus = user.getBuildings().getHe3Bonus();
        double metalBonus = user.getBuildings().getMetalBonus();

        List<UserBoost> boosts = user.getStats().getBuffs();

        for(UserBoost userBoost : boosts) {

            Optional<GameBoost> optionalGameBoost = userBoost.getGameBoost();

            if(optionalGameBoost.isEmpty())
                continue;

            GameBoost boost = optionalGameBoost.get();

            for(BonusType bonusType : boost.getBonuses()) {
                switch(bonusType) {
                    case BASIC_GOLD_RESOURCE_PRODUCTION:
                    case LUXURIOUS_GOLD_RESOURCE_PRODUCTION:
                    case ADVANCED_GOLD_RESOURCE_PRODUCTION:
                        goldBonus += bonusType.delta();
                        continue;
                    case BASIC_METAL_RESOURCE_PRODUCTION:
                    case METALLIC_METAL_RESOURCE_PRODUCTION:
                        metalBonus += bonusType.delta();
                        continue;
                    case BASIC_HE3_RESOURCE_PRODUCTION:
                    case GASEOUS_HE3_RESOURCE_PRODUCTION:
                        he3Bonus += bonusType.delta();
                        continue;
                    case GF_RESOURCE_PRODUCTION:
                    case MVP_RESOURCE_PRODUCTION:
                    case CHRISTMAS_RESOURCE_PRODUCTION:
                    case HALLOWEEN_RESOURCE_PRODUCTION:
                        goldBonus += bonusType.delta();
                        metalBonus += bonusType.delta();
                        he3Bonus += bonusType.delta();
                        continue;
                }
            }

        }

        int productionGold = (int) (gainGold * (1 + goldBonus));
        int productionMetal = (int) (gainMetal * (1 + metalBonus));
        int productionHe3 = (int) (gainHe3 * (1 + he3Bonus));

        user.getStorage().setGoldProduction(productionGold);
        user.getStorage().setMetalProduction(productionMetal);
        user.getStorage().setHe3Production(productionHe3);

        // user.getStorage().setLastProductionCalculus(DateUtil.now());

    }

    public ResponseBuyGoodsPacket getBuyGoodsPacket(PropData good, PropMallData method, int lockFlag, int quantity, int price) {

        ResponseBuyGoodsPacket response = new ResponseBuyGoodsPacket();

        response.setPropsId(good.getId());
        response.setPrice(price);
        response.setLockFlag((char) lockFlag);
        response.setNum(quantity);
        response.setCurrency((char) method.getCurrency());

        return response;

    }

    public static UserService getInstance() {
        return userService;
    }

}
