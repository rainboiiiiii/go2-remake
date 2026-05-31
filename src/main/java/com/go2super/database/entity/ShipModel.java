package com.go2super.database.entity;

import com.go2super.resources.ResourceManager;
import com.go2super.resources.data.ShipPartData;
import com.go2super.resources.data.meta.BodyLevelMeta;
import com.go2super.resources.data.meta.PartEffectMeta;
import com.go2super.resources.data.meta.PartLevelMeta;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.persistence.Column;
import javax.persistence.Id;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "game_models")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShipModel {

    @Id
    private ObjectId id;

    @Column(unique = true)
    private int shipModelId;
    private int guid;

    private String name;
    private int bodyId;

    private boolean deleted;

    private List<Integer> parts = new ArrayList<>();

    public int getBuildTime() {

        int result = 0;

        for(int part : parts) {

            PartLevelMeta meta = ResourceManager.getShipParts().getMeta(part);

            if(meta == null)
                continue;

            result += meta.getBuildCost().getTime();

        }

        BodyLevelMeta body = ResourceManager.getShipBodies().getMeta(bodyId);

        if(body != null)
            result += body.getBuildCost().getTime();

        return result;

    }

    public int getMetal() {

        int result = 0;

        for(int part : parts) {

            PartLevelMeta meta = ResourceManager.getShipParts().getMeta(part);

            if(meta == null)
                continue;

            result += meta.getBuildCost().getMetal();

        }

        System.out.println(result);

        BodyLevelMeta body = ResourceManager.getShipBodies().getMeta(bodyId);

        if(body != null)
            result += body.getBuildCost().getMetal();

        System.out.println(result);

        return result;

    }

    public int getHe3() {

        int result = 0;

        for(int part : parts) {

            PartLevelMeta meta = ResourceManager.getShipParts().getMeta(part);

            if(meta == null)
                continue;

            result += meta.getBuildCost().getFuel();

        }

        BodyLevelMeta body = ResourceManager.getShipBodies().getMeta(bodyId);

        if(body != null)
            result += body.getBuildCost().getFuel();

        return result;

    }

    public int getGold() {

        int result = 0;

        for(int part : parts) {

            PartLevelMeta meta = ResourceManager.getShipParts().getMeta(part);

            if(meta == null)
                continue;

            result += meta.getBuildCost().getGold();

        }

        BodyLevelMeta body = ResourceManager.getShipBodies().getMeta(bodyId);

        if(body != null)
            result += body.getBuildCost().getGold();

        return result;

    }

    public int getDurability() {
        return getStructure() + getShields();
    }

    public int getStructure() {

        int result = 0;

        for(int part : parts) {
            PartEffectMeta effect = ResourceManager.getShipParts().getEffect(part, "structure");
            result += effect != null ? (double) effect.getValue() : 0;
        }

        return result;

    }

    public int getShields() {

        int result = 0;

        for(int part : parts) {
            PartEffectMeta effect = ResourceManager.getShipParts().getEffect(part, "shield");
            result += effect != null ? (double) effect.getValue() : 0;
        }

        return result;

    }

    public int getMinRange() {

        int result = 0;

        for(int part : parts) {

            PartEffectMeta effect = ResourceManager.getShipParts().getEffect(part, "range");

            if(effect == null)
                continue;

            int range = (int) effect.getMin();

            if(result == 0)
                result = range;
            else if(result > range)
                result = range;

        }

        return result;

    }

    public int getMaxRange() {

        int result = 0;

        for(int part : parts) {

            PartEffectMeta effect = ResourceManager.getShipParts().getEffect(part, "range");

            if(effect == null)
                continue;

            int range = (int) effect.getMax();

            if(result == 0)
                result = range;
            else if(result < range)
                result = range;

        }

        return result;

    }

    public int getMinAttack() {

        int result = 0;

        for(int part : parts) {
            PartEffectMeta effect = ResourceManager.getShipParts().getEffect(part, "attack");
            result += effect != null ? effect.getMin() : 0;
        }

        return result;

    }

    public int getMaxAttack() {

        int result = 0;

        for(int part : parts) {
            PartEffectMeta effect = ResourceManager.getShipParts().getEffect(part, "attack");
            result += effect != null ? effect.getMax() : 0;
        }

        return result;

    }

    public int getMovement() {

        int result = 0;

        for(int part : parts) {
            PartEffectMeta effect = ResourceManager.getShipParts().getEffect(part, "movement");
            result += effect != null ? (double) effect.getValue() : 0;
        }

        return result;

    }

    public int[] partArray() {
        int[] array = new int[50];
        for(int i = 0; i < parts.size(); i++)
            array[i] = parts.get(i);
        return array;
    }

    public int partNum() {
        int count = 0;
        for(int part : parts)
            if(part > 0)
                count++;
        return count;
    }

}
