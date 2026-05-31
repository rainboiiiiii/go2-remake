package com.go2super.service;

import com.go2super.database.repository.GameBoostRepository;
import com.go2super.database.repository.UserRepository;
import com.go2super.packet.props.ResponseUsePropsPacket;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ResourcesService {

    private static ResourcesService resourcesService;

    @Autowired
    @Getter
    private UserRepository userRepository;

    @Autowired
    @Getter
    private GameBoostRepository boostRepository;

    public ResourcesService() {
        resourcesService = this;
    }

    public ResponseUsePropsPacket itemAward(int propId) {
        return itemAward(propId, 1);
    }

    public ResponseUsePropsPacket itemAward(int propId, int quantity) {
        return genericUseProps(propId, quantity, 1, 1);
    }

    public ResponseUsePropsPacket genericUseProps(int propId, int number, int lockFlag, int awardLockFlag) {

        ResponseUsePropsPacket packet = new ResponseUsePropsPacket();

        packet.setPropsId(propId);
        packet.setNumber(number);
        packet.setLockFlag((char) lockFlag);
        packet.setAwardLockFlag((char) awardLockFlag);

        return packet;

    }

    public static ResourcesService getInstance() {
        return resourcesService;
    }

}
