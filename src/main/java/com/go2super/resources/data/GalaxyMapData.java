package com.go2super.resources.data;

import com.go2super.obj.utility.GalaxyTile;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class GalaxyMapData {

    private int x;
    private int y;

    public GalaxyMapData(int x, int y) {

        this.x = x;
        this.y = y;

    }

    public GalaxyTile getTile() {
        return new GalaxyTile(x, y);
    }

}
