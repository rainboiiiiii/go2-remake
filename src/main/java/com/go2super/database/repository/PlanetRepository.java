package com.go2super.database.repository;

import com.go2super.database.entity.Planet;
import com.go2super.database.repository.custom.PlanetRepositoryCustom;
import com.go2super.obj.utility.GalaxyTile;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.io.Serializable;
import java.util.List;

public interface PlanetRepository extends MongoRepository<Planet, String>, PlanetRepositoryCustom, Serializable {

    Planet findTopByOrderByIdDesc();

    Planet findByPosition(GalaxyTile position);

    List<Planet> findAll();

    long count();

}