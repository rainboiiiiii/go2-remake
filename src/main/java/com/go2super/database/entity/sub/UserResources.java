package com.go2super.database.entity.sub;

import com.go2super.database.entity.User;
import com.go2super.obj.type.BonusType;
import com.go2super.socket.util.DateUtil;
import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Builder
@Data
public class UserResources {

    private static final int MAX_RESOURCES = 900000000;

    private int gold;
    private int he3;
    private int metal;

    private int vouchers;
    private int mallPoints;
    private int coupons;
    private int corsairs;

    private int honor;
    private int badge;
    private int championPoints;

    private int freeSpins;
    private Date lastSpin;

    public void addGold(int gold) {

        if(this.gold + gold > MAX_RESOURCES) {
            this.gold = MAX_RESOURCES;
            return;
        }

        this.gold += gold;

    }

    public void addHe3(int he3) {

        if(this.he3 + he3 > MAX_RESOURCES) {
            this.he3 = MAX_RESOURCES;
            return;
        }

        this.he3 += he3;

    }

    public void addMetal(int metal) {

        if(this.metal + metal > MAX_RESOURCES) {
            this.metal = MAX_RESOURCES;
            return;
        }

        this.metal += metal;

    }

    public void addVouchers(int vouchers) {

        if(this.vouchers + vouchers > MAX_RESOURCES) {
            this.vouchers = MAX_RESOURCES;
            return;
        }

        this.vouchers += vouchers;

    }

    public void removeSpin() {

        if(freeSpins > 0)
            this.freeSpins -= 1;

    }

}
