package com.go2super.database.entity.sub;

import com.go2super.database.entity.type.ExpertiseType;
import com.go2super.service.CommanderService;
import com.google.common.primitives.Chars;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommanderExpertise {

    private ExpertiseType ballistic = ExpertiseType.C;
    private ExpertiseType directional = ExpertiseType.C;
    private ExpertiseType missile = ExpertiseType.C;
    private ExpertiseType carrier = ExpertiseType.C;

    private ExpertiseType defend = ExpertiseType.C;
    private ExpertiseType frigate = ExpertiseType.C;
    private ExpertiseType cruiser = ExpertiseType.C;
    private ExpertiseType battleShip = ExpertiseType.C;

    public String getJZ() {
        return ballistic.name() + directional.name() + missile.name() + carrier.name() + defend.name() + frigate.name() + cruiser.name() + battleShip.name();
    }

    public static CommanderExpertise common() {

        CommanderExpertise expertise = new CommanderExpertise();
        List<Character> chars = Chars.asList(CommanderService.getInstance().getCommonExpertisePattern().toCharArray());

        Collections.shuffle(chars);

        expertise.setBallistic(ExpertiseType.getLiteral(chars.get(0)));
        expertise.setDirectional(ExpertiseType.getLiteral(chars.get(1)));
        expertise.setMissile(ExpertiseType.getLiteral(chars.get(2)));
        expertise.setCarrier(ExpertiseType.getLiteral(chars.get(3)));

        expertise.setDefend(ExpertiseType.getLiteral(chars.get(4)));
        expertise.setFrigate(ExpertiseType.getLiteral(chars.get(5)));
        expertise.setCruiser(ExpertiseType.getLiteral(chars.get(6)));
        expertise.setBattleShip(ExpertiseType.getLiteral(chars.get(7)));

        return expertise;

    }

}
