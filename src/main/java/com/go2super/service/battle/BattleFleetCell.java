package com.go2super.service.battle;

import com.go2super.database.entity.ShipModel;
import com.go2super.obj.game.ShipTeamNum;
import com.go2super.resources.ResourceManager;
import com.go2super.resources.data.ShipPartData;
import com.go2super.resources.data.meta.PartEffectMeta;
import com.go2super.service.PacketService;
import lombok.Data;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;

@Data
public class BattleFleetCell {

    private int guid = -1;

    private int shipModelId;
    private int amount;

    private int shields;
    private int structure;

    private int maxShields;
    private int maxStructure;

    private int movement;

    private List<BattleFleetWeapon> weapons = new ArrayList<>();

    public boolean isEmpty() {
        return shipModelId == -1 || amount == 0;
    }

    public List<Pair<Integer, ShipPartData>> getAttackParts() {

        List<Pair<Integer, ShipPartData>> parts = new ArrayList<>();
        ShipModel model = PacketService.getShipModel(shipModelId);

        for(int part : model.getParts()) {

            ShipPartData data = ResourceManager.getShipParts().findByPartId(part);

            if(data.getPartType().equals("attack"))
                parts.add(Pair.of(part, data));

        }

        return parts;

    }

    public static BattleFleetCell getByNum(int guid, ShipTeamNum num) {

        BattleFleetCell cell = new BattleFleetCell();

        cell.setGuid(guid);
        cell.setShipModelId(num.getShipModelId());
        cell.setAmount(num.getNum());

        if(cell.getAmount() > 0) {

            ShipModel model = PacketService.getShipModel(cell.getShipModelId());

            cell.setMovement(model.getMovement());
            cell.setWeapons(BattleFleetWeapon.getByNum(guid, cell));

        }

        return cell;

    }

}
