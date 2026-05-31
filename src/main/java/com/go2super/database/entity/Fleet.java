package com.go2super.database.entity;

import com.go2super.obj.game.ShipTeamBody;
import com.go2super.obj.game.ShipTeamNum;
import com.go2super.service.CommanderService;
import com.go2super.service.PacketService;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.persistence.Column;
import javax.persistence.Id;

@Document(collection = "game_fleets")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Fleet {

    @Id
    private ObjectId id;

    private int shipTeamId;
    private int galaxyId;

    @Column(unique = true)
    private int guid;
    private String name;

    private int commanderId;
    private int he3;

    private int bodyId;
    private int target;
    private int targetInterval;

    private int posX;
    private int posY;

    private int direction;

    private ShipTeamBody fleetBody;

    public void remove() {
        PacketService.getFleetRepository().delete(this);
    }

    public void save() {
        PacketService.getFleetRepository().save(this);
    }

    public Commander getCommander() {
        return CommanderService.getInstance().getCommander(commanderId);
    }

    public int bodyId() {

        int body = -1;

        for(ShipTeamNum teamNum : fleetBody.cells) {

            ShipModel model = PacketService.getShipModel(teamNum.getShipModelId());

            if(model == null)
                continue;

            if(body < model.getBodyId())
                body = model.getBodyId();

        }

        return body == -1 ? 0 : body;

    }

    public int getMaxHe3() {
        return ships() * 2; // todo
    }

    public int ships() {
        int number = 0;
        for(ShipTeamNum cell : fleetBody.getCells())
            if(cell.getShipModelId() >= 0)
                number += cell.getNum();
        return number;
    }

}
