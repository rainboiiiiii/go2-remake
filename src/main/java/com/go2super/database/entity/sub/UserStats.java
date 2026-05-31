package com.go2super.database.entity.sub;

import com.go2super.database.entity.GameBoost;
import com.go2super.database.entity.Planet;
import com.go2super.obj.type.BonusType;
import com.go2super.resources.ResourceManager;
import com.go2super.socket.util.DateUtil;
import lombok.Builder;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Builder
public class UserStats extends Planet {

    @Getter private int exp;

    @Getter private int maxSp;
    @Getter private int sp;

    @Getter private int kills;

    private List<UserBoost> buffs = new ArrayList<>();

    public boolean hasBonus(BonusType bonusType) {
        return getAllBonuses().contains(bonusType);
    }

    public int getLevel() {
        return ResourceManager.getLevels().getLevel(exp);
    }

    public List<BonusType> getAllBonuses() {
        List<BonusType> bonusList = new ArrayList<>();
        for(UserBoost userBoost : getBuffs())
            for(BonusType bonusType : userBoost.boost().getBonuses())
                bonusList.add(bonusType);
        return bonusList;
    }

    public void removeBoost(UserBoost userBoost) {
        buffs.remove(userBoost);
    }

    public boolean addBoost(GameBoost gameBoost) {

        addBonusTime(gameBoost, gameBoost.getSeconds());
        return true;

    }

    public void addBonusTime(GameBoost gameBoost, int seconds) {

        if(buffs == null)
            buffs = new ArrayList<>();

        UserBoost oldBoost = getUserBoost(gameBoost);

        if(oldBoost == null) {
            buffs.add(new UserBoost(gameBoost.getId().toString(), DateUtil.now(seconds)));
            return;
        }

        oldBoost.addSeconds(seconds);

    }

    public UserBoost getUserBoost(GameBoost gameBoost) {

        if(buffs == null)
            buffs = new ArrayList<>();

        for(UserBoost boost : buffs)
            if(boost.getGameBoostId().equals(gameBoost.getId().toString()))
                return boost;

        return null;

    }

    public void setBuffs(List<UserBoost> buffs) {
        this.buffs = buffs;
    }

    public List<UserBoost> getBuffs() {
        if(buffs == null)
            this.buffs = new ArrayList<>();
        return buffs;
    }

}
