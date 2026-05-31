package com.go2super.database.entity;

import com.go2super.database.entity.type.AccountStatus;
import com.go2super.database.entity.type.UserRank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.persistence.Column;
import javax.persistence.Id;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Document(collection = "game_accounts")
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Account {

    @Id
    private ObjectId id;

    @Column(unique = true) private String username;
    @Column(unique = true) private String email;

    private String password;
    private String lastIp;

    private int vip;


    private Date lastConnection;
    private Date registerDate;

    @Builder.Default
    private List<Long> userIds = new ArrayList<>();
    private AccountStatus accountStatus;

    private UserRank userRank;




}
