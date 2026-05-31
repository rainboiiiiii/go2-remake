package com.go2super.obj.utility;

import lombok.Data;

@Data
public class SmartString {

    private String value;
    private int size;

    public SmartString(int size) {
        this.size = size;
    }

    public String noSpaces() {
        return value.replaceAll("\\s", "").replaceAll(" ", "");
    }

    public SmartString value(String value) {
        this.value = value;
        return this;
    }

    public String shrink(int length) {
        return value.substring(0, length);
    }

    public static SmartString of(String value, int size) {
        return new SmartString(size).value(value);
    }

    public static SmartString of(int size) {
        return new SmartString(size);
    }

}
