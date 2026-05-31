package com.go2super.service.battle;

import com.go2super.database.entity.Match;
import lombok.Data;
import lombok.SneakyThrows;

@Data
public class MatchRunnable implements Runnable {

    private Match match;

    public MatchRunnable(Match match) {

        this.match = match;

    }

    public void update() {

    }

    @Override
    @SneakyThrows
    public void run() {

        while(true) {

            Thread.sleep(500L);
            match.next();

            // System.out.println("ONE");

        }

    }

}
