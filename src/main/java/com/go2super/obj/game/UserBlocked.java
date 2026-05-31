package com.go2super.obj.game;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;

import java.util.Date;

@Data
@Builder
@ToString
public class UserBlocked {

    private int fromGuid;
    private int toGuid;

    private long until;

}
