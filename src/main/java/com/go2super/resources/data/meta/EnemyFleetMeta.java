package com.go2super.resources.data.meta;

import com.go2super.resources.ResourceManager;
import com.go2super.resources.data.LayoutData;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class EnemyFleetMeta {

    private int x;
    private int y;

    private String layout;

    public LayoutData getLayoutData() {
        return ResourceManager.getInstances().getLayout(layout);
    }

}
