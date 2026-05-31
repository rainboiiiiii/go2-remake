package com.go2super.database.repository;

import com.go2super.database.entity.Corp;
import com.go2super.database.repository.custom.CorpRepositoryCustom;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.io.Serializable;
import java.util.List;

public interface CorpRepository extends MongoRepository<Corp, String>, CorpRepositoryCustom, Serializable {

    Corp findTopByOrderByIdDesc();

    List<Corp> findAll();

    Corp findByCorpId(int galaxyId);

    long count();

}