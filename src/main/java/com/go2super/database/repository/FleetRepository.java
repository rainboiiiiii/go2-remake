package com.go2super.database.repository;

import com.go2super.database.entity.Fleet;
import com.go2super.database.entity.ShipModel;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.io.Serializable;
import java.util.List;

public interface FleetRepository extends MongoRepository<Fleet, String>, Serializable {

    Fleet findTopByOrderByIdDesc();

    Fleet findByCommanderId(int commanderId);

    Fleet findByShipTeamId(int shipTeamId);

    List<Fleet> findAll();

    List<Fleet> findAllByGalaxyId(int galaxyId);

    List<Fleet> findAllByGuid(int guid);

    long count();

}