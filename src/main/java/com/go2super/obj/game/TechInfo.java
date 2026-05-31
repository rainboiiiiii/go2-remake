package com.go2super.obj.game;

import com.go2super.buffer.Go2Buffer;
import com.go2super.obj.BufferObject;
import com.go2super.obj.utility.UnsignedChar;
import com.go2super.obj.utility.UnsignedShort;
import lombok.Data;

@Data
public class TechInfo extends BufferObject {

    public short techId;
    public short levelId;

    public TechInfo(int techId, int levelId) {
        this.techId = (short) techId;
        this.levelId = (short) levelId;
    }

    @Override
    public void write(Go2Buffer go2buffer) {

        go2buffer.addShort(techId);
        go2buffer.addShort(levelId);

    }

}
