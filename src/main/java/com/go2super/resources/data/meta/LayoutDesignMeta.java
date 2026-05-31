package com.go2super.resources.data.meta;

import com.go2super.database.entity.ShipModel;
import com.go2super.resources.ResourceManager;
import com.go2super.resources.data.DefaultModelData;
import com.go2super.service.PacketService;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class LayoutDesignMeta {

    private String design;
    private int amount;

    public ShipModel getModel() {

        DefaultModelData defaultModel = getDefaultModel();

        if(defaultModel == null)
            return PacketService.getShipModel(0);

        return PacketService.getShipModel(defaultModel.getId());

    }

    public DefaultModelData getDefaultModel() {
        return ResourceManager.getShipModels().lookupDefaultId(design);
    }

}
