package com.go2super.obj.game;

import com.go2super.buffer.Go2Buffer;
import com.go2super.obj.BufferObject;
import com.go2super.obj.utility.UnsignedChar;
import com.go2super.obj.utility.UnsignedShort;
import lombok.Data;

@Data
public class BuildInfo extends BufferObject {

    public int spareTime;

    public UnsignedShort posX;
    public UnsignedShort posY;
    public UnsignedShort indexId;
    public UnsignedChar buildingId;

    public char levelId;

    public BuildInfo(int spareTime, short posX, short posY, short indexId, char buildingId, char levelId) {
        this.spareTime = spareTime;

        this.posX = new UnsignedShort(posX);
        this.posY = new UnsignedShort(posY);
        this.indexId = new UnsignedShort(indexId);
        this.buildingId = new UnsignedChar(buildingId);

        this.levelId = levelId;

    }

    public BuildInfo(int spareTime, int posX, int posY, int indexId, int buildingId, int levelId) {
        this.spareTime = spareTime;

        this.posX = new UnsignedShort(posX);
        this.posY = new UnsignedShort(posY);
        this.indexId = new UnsignedShort(indexId);
        this.buildingId = new UnsignedChar(buildingId);

        this.levelId = (char) levelId;

    }

    @Override
    public void read(Go2Buffer go2buffer) { }

    @Override
    public void write(Go2Buffer go2buffer) {

        go2buffer.addInt(spareTime);

        go2buffer.addUnsignShort(posX.getValue());
        go2buffer.addUnsignShort(posY.getValue());
        go2buffer.addUnsignShort(indexId.getValue());
        go2buffer.addUnsignChar(buildingId.getValue());

        go2buffer.addChar(levelId);

    }

}
