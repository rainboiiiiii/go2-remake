package com.go2super.resources.data;

import lombok.Data;
import lombok.ToString;

import java.util.List;

@Data
@ToString
public class DefaultModelData {

    private int id;
    private String name;

    private int bodyId;
    private List<Integer> parts;

    public int[] getPartsArray() {
        int[] array = new int[parts.size()];
        for(int i = 0; i < array.length; i++)
            array[i] = parts.get(i);
        return array;
    }

}
