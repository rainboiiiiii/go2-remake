package com.go2super.obj.game;

import com.go2super.buffer.Go2Buffer;
import com.go2super.obj.BufferObject;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GalaxyFleetInfo extends BufferObject {

    private int shipTeamId;
    private int shipNum;

    private short bodyId;
    private short reserve;

    private char direction;
    private char posX;
    private char posY;

    private char owner;

    @Override
    public void write(Go2Buffer go2buffer) {

        go2buffer.addInt(shipTeamId);
        go2buffer.addInt(shipNum);

        go2buffer.addShort(bodyId);
        go2buffer.addShort(reserve);

        go2buffer.addChar(direction);
        go2buffer.addChar(posX);
        go2buffer.addChar(posY);

        go2buffer.addChar(owner);

    }

    @Override
    public GalaxyFleetInfo trash() {
        return new GalaxyFleetInfo();
    }

}