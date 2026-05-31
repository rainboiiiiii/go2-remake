package com.go2super.database.repository;

import com.go2super.database.entity.User;
import com.go2super.database.repository.custom.UserRepositoryCustom;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends MongoRepository<User, String>, UserRepositoryCustom, Serializable {

    User findTopByOrderByIdDesc();

    List<User> findAll();

    User findByGuid(int guid);

    User findByUserId(long userId);

    Optional<User> findByUsername(String username);

}