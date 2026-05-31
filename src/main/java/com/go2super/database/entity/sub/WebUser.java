package com.go2super.database.entity.sub;

import com.go2super.database.entity.type.UserRank;
import com.go2super.socket.util.MathUtil;
import lombok.Builder;
import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.persistence.Column;
import javax.persistence.Id;
import java.util.Date;

@Builder
@Data
public class WebUser {

    @Id
    private ObjectId id;

    private String email;
    private String username;
    private String password;
    private UserRank userRank;

    private int icon;
    private int vip;

    private String lastIp;
    private Date lastConnection;
    private Date registerDate;

}
