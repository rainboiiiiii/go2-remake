package com.go2super.resources.data;

import com.go2super.resources.data.meta.LotteryRewardMeta;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class LotteryData {

    private int id;
    private int position;
    private String name;
    private boolean broadcast;
    private int weight;

    private LotteryRewardMeta reward;

}
