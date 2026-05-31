package com.go2super.database.entity.sub;

import com.go2super.database.entity.Commander;
import com.go2super.service.CommanderService;
import lombok.Data;

@Data
public class BattleCommander {

    private int commanderId;

    private String name;

    private int stars;
    private int level;

    private double totalAim;
    private double totalDodge;
    private double totalSpeed;
    private double totalElectron;

    private CommanderExpertise expertise;

    public Commander getCommander() {
        return CommanderService.getInstance().getCommander(commanderId);
    }

}
