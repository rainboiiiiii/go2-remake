package com.go2super.resources.data.meta;

import lombok.Data;
import lombok.ToString;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Data
@ToString
public class LotteryRewardMeta {

    private String type;

    private int id;
    private int[] ids;

    private int amount;

    public int pickOne() {

        List<Integer> list = new ArrayList<>();

        for(int i = 0; i < ids.length; i++)
            list.add(ids[i]);

        Collections.shuffle(list);
        return list.get(0);

    }

}
