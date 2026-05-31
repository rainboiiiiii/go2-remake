package com.go2super.obj.game;

import com.go2super.buffer.Go2Buffer;
import com.go2super.obj.BufferObject;
import com.go2super.resources.ResourceManager;
import com.go2super.resources.data.PropData;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class Prop extends BufferObject {

    private int propId;
    private int propNum;
    private int propLockNum;

    private int storageType;
    private int reserve;

    public Prop(int propId, int propNum, int propLockNum, int storageType, int reserve) {
        this.propId = propId;
        this.propNum = propNum;
        this.propLockNum = propLockNum;
        this.storageType = storageType;
        this.reserve = reserve;
    }

    public PropData getData() {
        return ResourceManager.getProps().getData(propId);
    }

    @Override
    public void write(Go2Buffer go2buffer) {

        go2buffer.pushByte((4 - go2buffer.getBuffer().position() % 4) % 4);
        go2buffer.addShort(propId);
        go2buffer.addShort(propNum);
        go2buffer.addShort(propLockNum);
        go2buffer.addChar(storageType);
        go2buffer.addChar(reserve);

    }

}
