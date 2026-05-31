package com.go2super.obj.utility;

import lombok.Data;

@Data
public class UnsignedInteger {

    private int value;

    public UnsignedInteger(int value) {
        this.value = value;
    }

    public static UnsignedInteger of(int value) { return new UnsignedInteger(value); }

}
