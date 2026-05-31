package com.go2super.database.entity.sub;

import com.go2super.obj.game.BuildInfo;
import com.go2super.resources.ResourceManager;
import com.go2super.resources.data.BuildData;
import com.go2super.resources.data.meta.BuildLevelMeta;
import com.go2super.socket.util.DateUtil;
import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Data
@Builder
public class UserBuilding {

    private Boolean repairing;
    private Date untilRepair;

    private Boolean updating;
    private Date untilUpdate;

    private int levelId;
    private int buildingId;

    private int x;
    private int y;

    public Long updatingTime() {

        if(updating == null || !updating || untilUpdate == null)
            return Long.valueOf(0);

        return DateUtil.remains(untilUpdate);

    }

    public Long repairingTime() {

        if(repairing == null || !repairing || untilRepair == null)
            return Long.valueOf(0);

        return DateUtil.remains(untilRepair);

    }

    public Long spareTime() {

        Long repairingTime = repairingTime();

        if(repairingTime != 0)
            return repairingTime;

        return updatingTime();

    }

    public BuildLevelMeta getLevelData() {
        return getData().getLevel(levelId);
    }

    public BuildData getData() {

        BuildData data = ResourceManager.getBuilds().getBuild(buildingId);
        return data;

    }

    public BuildInfo getInfo(int spareTime, int index) {
        return new BuildInfo(spareTime, x, y, index, buildingId, levelId);
    }

}
