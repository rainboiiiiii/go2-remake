package com.go2super.service;

import com.go2super.packet.PacketRouter;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
public class GameNetworkService {

    private PacketRouter packetRouter;

    @PostConstruct
    public void init() {
        packetRouter = new PacketRouter();
        packetRouter.craftPackets();
        packetRouter.craftListeners();
    }

    public PacketRouter getPacketRouter() {
        return packetRouter;
    }

}
