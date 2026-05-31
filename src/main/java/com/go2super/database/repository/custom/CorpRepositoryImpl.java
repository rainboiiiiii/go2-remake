package com.go2super.database.repository.custom;

import com.go2super.database.entity.Corp;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;

import java.util.List;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;

public class CorpRepositoryImpl implements CorpRepositoryCustom {

    @Autowired
    MongoTemplate mongoTemplate;

    @Override
    public List<Corp> findByPower(int page, int max) {

        Aggregation aggregation = newAggregation(Corp.class,
            sort(Sort.Direction.DESC, "planets"),
            sort(Sort.Direction.DESC, "contribution"),
            sort(Sort.Direction.DESC, "exp"),
            limit(max),
            skip(page <= 0 ? 0 : page * max));

        AggregationResults<Corp> results = mongoTemplate.aggregate(aggregation, Corp.class, Corp.class);
        List<Corp> result = results.getMappedResults();

        return result;

    }

}
