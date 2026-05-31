package com.go2super.database.entity.sub;

import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Builder
@Data
public class UserStorage {

    private int gold;
    private int maxGold;

    private int he3;
    private int maxHe3;

    private int metal;
    private int maxMetal;

    private int goldProduction;
    private int he3Production;
    private int metalProduction;

    private Date lastProductionCalculus;

    public void reset() {

        this.gold = 0;
        this.he3 = 0;
        this.metal = 0;

    }

    public void addGold(int gold) {

        if(this.gold + gold > this.maxGold) {
            this.gold = this.maxGold;
            return;
        }

        this.gold += gold;

    }

    public void addHe3(int he3) {

        if(this.he3 + he3 > this.maxHe3) {
            this.he3 = this.maxHe3;
            return;
        }

        this.he3 += he3;

    }

    public void addMetal(int metal) {

        if(this.metal + metal > this.maxMetal) {
            this.metal = this.maxMetal;
            return;
        }

        this.metal += metal;

    }

}
