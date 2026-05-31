package com.go2super.socket.data;

import java.util.Map;

public interface DiscordConsumer {

    void send(Map<String, Object> packet);

}
