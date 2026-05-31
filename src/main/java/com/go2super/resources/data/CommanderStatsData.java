package com.go2super.resources.data;

import com.go2super.database.entity.sub.CommanderExpertise;
import com.go2super.database.entity.type.ExpertiseType;
import com.go2super.resources.localization.Localization;
import lombok.Data;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;

@Data
@ToString
public class CommanderStatsData {

    private String name;
    private int type;
    private int commanderType;

    private String skillName;
    private String comment;
    private String description;

    private int aim;
    private int blench;
    private int priority;
    private int electron;

    private ExpertiseType ballistic;
    private ExpertiseType directional;
    private ExpertiseType missile;
    private ExpertiseType carrier;
    private ExpertiseType defend;
    private ExpertiseType frigate;
    private ExpertiseType cruiser;
    private ExpertiseType battleShip;

    private String imageFileName;
    private String image;

    public String getName() {
        return Localization.EN_US.get(name);
    }

    public CommanderExpertise getExpertise() {
        return CommanderExpertise.builder()
                .ballistic(ballistic)
                .directional(directional)
                .missile(missile)
                .carrier(carrier)
                .defend(defend)
                .frigate(frigate)
                .cruiser(cruiser)
                .battleShip(battleShip)
                .build();
    }

}
