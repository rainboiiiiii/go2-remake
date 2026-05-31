package com.go2super.obj.game;

import com.go2super.buffer.Go2Buffer;
import com.go2super.obj.BufferObject;
import lombok.Data;

@Data
public class TechUpgradeInfo extends BufferObject {

    public int needTime;
    public short techId;
    public short creditFlag;

    public TechUpgradeInfo(int needTime, int techId, int creditFlag) {
        this.needTime = needTime;
        this.techId = (short) techId;
        this.creditFlag = (short) creditFlag;
    }

    @Override
    public void write(Go2Buffer go2buffer) {

        go2buffer.addInt(needTime);

        go2buffer.addShort(techId);
        go2buffer.addShort(creditFlag);

    }

}
