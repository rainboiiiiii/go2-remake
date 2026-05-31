package com.go2super.database.repository.custom;

import com.go2super.database.entity.Corp;
import com.go2super.database.entity.User;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.List;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.skip;

public class UserRepositoryImpl implements UserRepositoryCustom {

    @Autowired
    MongoTemplate mongoTemplate;

    @Override
    public List<User> findByPower(int page, int max) {

        Aggregation aggregation = newAggregation(Corp.class,
                sort(Sort.Direction.DESC, "game_ships.ships.attack"),
                limit(max),
                skip(page <= 0 ? 0 : page * max));

        AggregationResults<User> results = mongoTemplate.aggregate(aggregation, User.class, User.class);
        List<User> result = results.getMappedResults();

        return result;

    }

    @Override
    public User getByLoginCredential(String credential) {

        Criteria criteria = new Criteria();
        criteria.orOperator(Criteria.where("web_user.email").is(credential), Criteria.where("web_user.username").is(credential));

        Query query = new Query(criteria);
        return mongoTemplate.findOne(query, User.class);

    }

}
