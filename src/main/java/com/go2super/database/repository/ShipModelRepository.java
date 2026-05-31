package com.go2super.database.repository;

import com.go2super.database.entity.ShipModel;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.io.Serializable;
import java.util.List;

public interface ShipModelRepository extends MongoRepository<ShipModel, String>, Serializable {

    ShipModel findTopByOrderByIdDesc();

    List<ShipModel> findAll();

    List<ShipModel> findAllByGuidAndDeleted(int guid, boolean deleted);

    ShipModel findByShipModelId(int shipModelId);

    ShipModel findByGuid(int guid);

    long count();

}