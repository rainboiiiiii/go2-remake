package com.go2super.obj.game;

import com.go2super.buffer.Go2Buffer;
import com.go2super.obj.BufferObject;
import com.go2super.obj.utility.SmartString;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CommanderBaseInfo extends BufferObject {

    private SmartString name = SmartString.of(32);

    private long userId;

    private int commanderId;
    private int shipTeamId;
    private int state;

    private short skill;

    private char level;
    private char type;

    public CommanderBaseInfo(String name, long userId, int commanderId, int shipTeamId, int state, int skill, int level, int type) {

        this.name.value(name);
        this.userId = userId;

        this.commanderId = commanderId;
        this.shipTeamId = shipTeamId;
        this.state = state;
        this.skill = (short) skill;
        this.level = (char) level;
        this.type = (char) type;

    }

    @Override
    public void write(Go2Buffer go2buffer) {

        go2buffer.addString(name.getValue(), name.getSize());
        go2buffer.addLong(userId);

        go2buffer.addInt(commanderId);
        go2buffer.addInt(shipTeamId);
        go2buffer.addInt(state);

        go2buffer.addShort(skill);
        go2buffer.addChar(level);
        go2buffer.addChar(type);

    }

    @Override
    public CommanderBaseInfo trash() {
        return new CommanderBaseInfo("", 0, 0, 0, 0, 0, 0, 0);
    }

}
