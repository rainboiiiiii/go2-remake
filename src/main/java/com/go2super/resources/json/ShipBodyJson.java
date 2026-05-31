package com.go2super.resources.json;

import com.go2super.resources.data.ShipBodyData;
import com.go2super.resources.data.ShipPartData;
import com.go2super.resources.data.meta.BodyLevelMeta;
import com.go2super.resources.data.meta.PartEffectMeta;
import com.go2super.resources.data.meta.PartLevelMeta;
import lombok.Data;
import lombok.ToString;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@ToString
public class ShipBodyJson {

    private Map<Integer, ShipBodyData> cachedBodies = new HashMap<>();
    private List<ShipBodyData> shipBody;

    public ShipBodyData findByBodyId(int id) {

        if(cachedBodies.containsKey(id))
            return cachedBodies.get(id);

        for(ShipBodyData data : shipBody)
            for(BodyLevelMeta meta : data.getLevels())
                if(meta.getId() == id) {
                    cachedBodies.put(id, data);
                    return data;
                }

        return null;

    }

    public BodyLevelMeta getMeta(int id) {
        return findByBodyId(id).getLevel(id);
    }

}
