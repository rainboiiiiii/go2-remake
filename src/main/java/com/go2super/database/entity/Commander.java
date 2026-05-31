package com.go2super.database.entity;

import com.go2super.database.entity.sub.BattleCommander;
import com.go2super.database.entity.sub.BionicChip;
import com.go2super.database.entity.sub.CommanderExpertise;
import com.go2super.database.entity.sub.UserPlanet;
import com.go2super.obj.game.CommanderAttributes;
import com.go2super.obj.game.CommanderLevel;
import com.go2super.resources.ResourceManager;
import com.go2super.resources.data.CommanderStatsData;
import com.go2super.resources.data.PropData;
import com.go2super.resources.data.meta.GemMeta;
import com.go2super.resources.data.props.PropGemData;
import com.go2super.resources.localization.Localization;
import com.go2super.service.CommanderService;
import com.go2super.service.GalaxyService;
import com.go2super.service.PacketService;
import com.go2super.socket.util.MathUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.persistence.Column;
import javax.persistence.Id;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Document(collection = "game_commanders")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Commander {

    @Id
    private ObjectId id;

    @Column(unique = true) private int commanderId;

    private long userId;
    private int shipTeamId = -1;

    private String name;

    private int skill;
    private int stars;
    private int experience;
    private int variance;

    private int state;
    private int restTime;

    private int growthAim;
    private int growthDodge;
    private int growthSpeed;
    private int growthElectron;

    private List<Integer> gems = new ArrayList<>();
    private List<BionicChip> chips = new ArrayList<>();

    private int commonBaseAim;
    private int commonBaseDodge;
    private int commonBaseSpeed;
    private int commonBaseElectron;

    private CommanderExpertise commonExpertise;

    public int propId() {
        return CommanderService.getInstance().getPropId(this);
    }

    public void save() {
        CommanderService.getInstance().getCommanderRepository().save(this);
    }

    public void reset() {

        if(isCommon()) {

            setCommonBaseAim(MathUtil.random(0, 11));
            setCommonBaseElectron(MathUtil.random(0, 11));
            setCommonBaseDodge(MathUtil.random(0, 11));
            setCommonBaseSpeed(MathUtil.random(0, 11));

            setCommonExpertise(CommanderExpertise.common());

        }

        this.experience = 0;

        randomizeGrowth();
        randomizeVariance();

    }

    public BattleCommander createBattleCommander() {

        BattleCommander commander = new BattleCommander();
        CommanderAttributes attributes = CommanderService.getInstance().getAttributes(this);

        commander.setCommanderId(commanderId);
        commander.setName(Localization.EN_US.get(name));
        commander.setStars(stars);
        commander.setTotalAim(attributes.getAim());
        commander.setTotalDodge(attributes.getDodge());
        commander.setTotalSpeed(attributes.getSpeed());
        commander.setTotalElectron(attributes.getElectron());
        commander.setLevel(getLevel().getLevel());

        return commander;

    }

    public Fleet getFleet() {
        return PacketService.getFleetRepository().findByCommanderId(commanderId);
    }

    public boolean hasFleet() {
        return getFleet() != null;
    }

    public int getStoneAim() {
        return (int) getAttribute("aim");
    }

    public int getStoneDodge() {
        return (int) getAttribute("blench");
    }

    public int getStoneElectron() {
        return (int) getAttribute("electron");
    }

    public int getStonePriority() {
        return (int) getAttribute("speed");
    }

    public int getStoneAssault() {
        return (int) (getAttribute("attack") * 1000);
    }

    public int getStoneEndure() {
        return (int) (getAttribute("structure") * 1000);
    }

    public int getStoneShield() {
        return (int) (getAttribute("shield") * 1000);
    }

    public int getStoneBlastHurt() {
        return (int) (getAttribute("critAttack") * 1000);
    }

    public int getStoneDoubleHit() {
        return (int) (getAttribute("doubleRate") * 1000);
    }

    public int getStoneRepairShield() {
        return (int) (getAttribute("shieldHeal") * 1000);
    }

    public int getStoneExp() {
        return (int) (getAttribute("xpRate") * 1000);
    }

    public double getAttribute(String name) {

        List<Integer> gems = getGems();
        double result = 0;

        for(int i = 0; i < getLevel().getLevelData().getGem(); i++) {

            int gemId = gems.get(i);

            if(gemId == -1)
                continue;

            PropData gem = ResourceManager.getProps().getGemData(gemId);

            if(gem == null)
                continue;

            PropGemData data = gem.getGemData();

            if(data.getStats() != null && data.getStats().size() > 0)
                for(GemMeta meta : data.getStats())
                    if(meta.getEffect().equals(name))
                        result += meta.getAdd();

        }

        return result;

    }

    public CommanderLevel getLevel() {
        return CommanderService.getInstance().getLevel(this);
    }

    public void delete() {
        CommanderService.getInstance().getCommanderRepository().delete(this);
    }

    public boolean isCommon() {
        return skill == -1;
    }

    public boolean isTitan() {
        return Arrays.asList(85, 86, 87, 88, 89, 90, 94, 105, 106).contains(skill);
    }

    public void randomizeGrowth() {
        CommanderService.getInstance().randomizeGrowth(this);
    }

    public void randomizeVariance() {
        CommanderService.getInstance().randomizeVariance(this);
    }

    public int getBaseAim() {
        return isCommon() ? commonBaseAim : getStats().getAim();
    }

    public int getBaseElectron() {
        return isCommon() ? commonBaseElectron : getStats().getElectron();
    }

    public int getBaseDodge() {
        return isCommon() ? commonBaseDodge : getStats().getBlench();
    }

    public int getBaseSpeed() {
        return isCommon() ? commonBaseSpeed : getStats().getPriority();
    }

    public String getName() {
        return isCommon() ? name : getStats().getName();
    }

    public int getType() {
        return isCommon() ? 1 : getStats().getType();
    }

    public User getUser() {
        return getUserPlanet().getUser();
    }

    public UserPlanet getUserPlanet() {
        return GalaxyService.getInstance().getUserPlanet(userId);
    }

    public CommanderExpertise getExpertise() {

        if(isCommon())
            return commonExpertise;

        return getStats().getExpertise();

    }

    public List<Integer> getGems() {
        if(gems == null || gems.size() != 12)
            for(int i = 0; i < 12; i++)
                gems.add(-1);
        return gems;
    }

    public boolean hasGems() {
        for(int gem : getGems())
            if(gem != -1)
                return true;
        return false;
    }

    public boolean hasChips() {
        for(int gem : getGems())
            if(gem != -1)
                return true;
        return false;
    }

    public CommanderAttributes getAttributes() {
        return CommanderService.getInstance().getAttributes(this);
    }

    public CommanderStatsData getStats() {
        return CommanderService.getInstance().getStats(this);
    }

}
