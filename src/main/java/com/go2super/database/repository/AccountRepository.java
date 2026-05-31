package com.go2super.database.repository;

import com.go2super.database.entity.Account;
import com.go2super.database.entity.User;
import com.go2super.database.repository.custom.UserRepositoryCustom;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

public interface AccountRepository extends MongoRepository<Account, String>, Serializable {

    List<Account> findAll();

    Optional<Account> findByEmail(String email);

    Optional<Account> findByUsername(String username);
}