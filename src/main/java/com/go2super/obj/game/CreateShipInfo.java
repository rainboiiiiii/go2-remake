package com.go2super.obj.game;

import com.go2super.buffer.Go2Buffer;
import com.go2super.obj.BufferObject;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CreateShipInfo extends BufferObject {

    public int shipModelId;
    public int needTime;
    public int num;
    public int incSpeed;

    public CreateShipInfo(int shipModelId, int needTime, int num, int incSpeed) {

        this.shipModelId = shipModelId;
        this.needTime = needTime;
        this.num = num;
        this.incSpeed = incSpeed;

    }

    @Override
    public void write(Go2Buffer go2buffer) {

        go2buffer.pushByte((4 - go2buffer.getBuffer().position() % 4) % 4);

        go2buffer.addInt(shipModelId);
        go2buffer.addInt(needTime);
        go2buffer.addInt(num);
        go2buffer.addInt(incSpeed);

    }

    @Override
    public CreateShipInfo trash() {
        return new CreateShipInfo(-1, 0, 0, 0);
    }

}