package com.go2super.obj.game;

import com.go2super.buffer.Go2Buffer;
import com.go2super.obj.BufferObject;
import lombok.Data;

@Data
public class CharArray extends BufferObject {

    private int[] array;

    public CharArray(int[] array) {
        this.array = array;
    }

    public CharArray(int capacity) {
        this.array = new int[capacity];
    }

    public void add(int...values) {
        for(int i = 0; i < values.length; i++)
            array[i] = values[i];
    }

    public void set(int pos, int value) {
        array[pos] = value;
    }

    @Override
    public void read(Go2Buffer buffer) {

        for(int i = 0; i < array.length; i++)
            array[i] = buffer.getChar();

    }

    @Override
    public void write(Go2Buffer buffer) {

        for(int value : array)
            buffer.addChar(value);

    }

}
