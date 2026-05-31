package com.go2super.service;

import com.go2super.database.entity.Fleet;
import com.go2super.database.entity.Planet;
import com.go2super.database.entity.ShipModel;
import com.go2super.database.entity.User;
import com.go2super.database.entity.sub.UserShips;
import com.go2super.database.repository.FleetRepository;
import com.go2super.database.repository.ShipModelRepository;
import com.go2super.database.repository.UserRepository;
import com.go2super.obj.game.ShipTeamNum;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PacketService {

    private static PacketService instance;

    private static Map<Integer, ShipModel> cachedModels = new HashMap<>();

    @Autowired
    @Getter
    private UserRepository userRepo;

    @Autowired
    @Getter
    private ShipModelRepository shipModelRepo;

    @Autowired
    @Getter
    private FleetRepository fleetRepo;

    @Value("${application.game.ip}")
    @Getter
    private String serverIp;

    @Value("${application.game.timeout}")
    @Getter
    private int timeout;

    @Value("${application.game.interval}")
    @Getter
    private int interval;

    public PacketService() {
        instance = this;
    }

    public int getNextShipModelId() {

        ShipModel last = shipModelRepo.findTopByOrderByIdDesc();

        if(last == null)
            return 1;

        return last.getShipModelId() + 1;

    }

    public int getNextShipTeamId() {

        Fleet last = fleetRepo.findTopByOrderByIdDesc();

        if(last == null)
            return 1;

        return last.getShipTeamId() + 1;

    }

    public static ShipModel getShipModel(int shipModel) {

        if(cachedModels.containsKey(shipModel))
            return cachedModels.get(shipModel);

        ShipModel model = getShipModelRepository().findByShipModelId(shipModel);

        if(model == null)
            return null;

        cachedModels.put(shipModel, model);
        return model;

    }

    public List<ShipTeamNum> getAllShipNums(User user) {

        List<ShipTeamNum> result = new ArrayList<>();
        result.addAll(user.getShips().getShips());

        for(Fleet fleet : PacketService.getFleetRepository().findAllByGuid(user.getGuid()))
            result.addAll(fleet.getFleetBody().getCells());

        return result;

    }

    public static ShipModelRepository getShipModelRepository() {
        return instance.getShipModelRepo();
    }

    public static UserRepository getUserRepository() {
        return instance.getUserRepo();
    }

    public static FleetRepository getFleetRepository() {
        return instance.getFleetRepo();
    }

    public static PacketService getInstance() {
        return instance;
    }

}
