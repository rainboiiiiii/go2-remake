package com.go2super.obj.model;

import com.go2super.database.entity.Account;
import com.go2super.database.entity.User;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoggedSessionAccount {

    private Account account;
    private String token;

}