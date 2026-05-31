package com.go2super.database.repository;

import com.go2super.database.entity.Commander;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.io.Serializable;
import java.util.List;

public interface CommanderRepository extends MongoRepository<Commander, String>, Serializable {

    Commander findTopByOrderByIdDesc();

    Commander findBySkillAndUserId(int skill, long userId);

    Commander findByCommanderIdAndUserId(int commanderId, long userId);

    Commander findByCommanderId(int commanderId);

    Commander findByShipTeamId(int shipTeamId);

    List<Commander> findByUserId(long userId);

    long count();

}