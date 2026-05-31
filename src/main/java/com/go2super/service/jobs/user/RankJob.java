package com.go2super.service.jobs.user;

import com.go2super.database.entity.User;
import com.go2super.obj.game.UserLeaderboard;
import com.go2super.service.RankService;
import com.go2super.service.UserService;
import com.go2super.service.jobs.OfflineJob;
import lombok.SneakyThrows;

import java.util.ArrayList;
import java.util.List;

public class RankJob implements OfflineJob {

    public List<User> toRankAdd = new ArrayList<>();

    boolean setup = false;

    @Override
    @SneakyThrows
    public void setup() {

        RankService.getInstance().setup();
        setup = true;

    }

    @Override
    @SneakyThrows
    public void run() {

        if(!setup)
            setup();

        for(UserLeaderboard userLeaderboard : RankService.getCache()) {

            User user = UserService.getInstance().getUserRepository().findByGuid(userLeaderboard.getGuid());

            if(user == null) {

                RankService.deleteUser(userLeaderboard);
                continue;

            }

            RankService.getInstance().update(user);
            Thread.sleep(1);

        }

        for(User user : toRankAdd)
            RankService.getInstance().add(user);

        toRankAdd.clear();
        RankService.getInstance().sort();

    }

    public void synchronizedAdd(User user) {
        toRankAdd.add(user);
    }

}
