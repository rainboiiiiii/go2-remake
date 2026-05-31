package com.go2super.obj.game;

import com.go2super.buffer.Go2Buffer;
import com.go2super.obj.BufferObject;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class ShipTeamBody extends BufferObject {

    public List<ShipTeamNum> cells = new ArrayList<>();

    public int getRenderedBody() {

        int bodyId = 0;

        for(ShipTeamNum cell : cells)
            if(bodyId < cell.getBodyId())
                bodyId = cell.getBodyId();

        return bodyId;

    }

    @Override
    public void write(Go2Buffer go2buffer) {

        for(int i = 0; i < 9; i++)
            if(cells.size() >= i) {

                ShipTeamNum num = cells.get(i);

                go2buffer.addInt(num.getShipModelId());
                go2buffer.addInt(num.getBodyId());
                go2buffer.addInt(num.getNum());

            } else {

                go2buffer.addInt(-1);
                go2buffer.addInt(0);
                go2buffer.addInt(0);

            }

    }

    @Override
    public void read(Go2Buffer go2buffer) {

        for(int i = 0; i < 9; i++) {
            ShipTeamNum teamNum = new ShipTeamNum(go2buffer.getInt(), go2buffer.getInt());
            cells.add(teamNum);
        }

    }

    @Override
    public ShipTeamBody trash() {
        return new ShipTeamBody();
    }

}