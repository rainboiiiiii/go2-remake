package com.go2super.resources.data.meta;

import com.go2super.database.entity.sub.BattleCommander;
import com.go2super.resources.localization.Localization;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class LayoutCommanderMeta {

    private String name;

    private int rank;
    private int lv;
    private int accuracy;
    private int dodge;
    private int speed;
    private int electron;

    public BattleCommander getBattleCommander() {

        BattleCommander commander = new BattleCommander();

        commander.setCommanderId(-1);
        commander.setName(Localization.EN_US.get(name));
        commander.setStars(rank);
        commander.setTotalAim(accuracy);
        commander.setTotalDodge(dodge);
        commander.setTotalSpeed(speed);
        commander.setTotalElectron(electron);
        commander.setLevel(lv);

        return commander;

    }

}
