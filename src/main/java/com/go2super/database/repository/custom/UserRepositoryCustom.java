package com.go2super.database.repository.custom;

import com.go2super.database.entity.Corp;
import com.go2super.database.entity.User;
import com.go2super.database.entity.sub.UserPlanet;
import com.go2super.obj.utility.GalaxyRegion;
import com.go2super.obj.utility.GameRegion;

import java.util.List;

public interface UserRepositoryCustom {

    List<User> findByPower(int page, int max);

    User getByLoginCredential(String credential);

}
