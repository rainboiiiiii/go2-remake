package com.go2super.service;

import com.go2super.database.entity.Fleet;
import com.go2super.database.entity.Match;
import com.go2super.database.entity.User;
import com.go2super.database.entity.sub.BattleFleet;
import com.go2super.database.entity.sub.BattleTag;
import com.go2super.database.entity.type.MatchType;
import com.go2super.database.repository.MatchRepository;
import com.go2super.database.repository.UserRepository;
import com.go2super.resources.ResourceManager;
import com.go2super.resources.data.InstanceData;
import com.go2super.resources.data.meta.PlayerFleetMeta;
import com.go2super.service.battle.BattleFleetTeam;
import com.go2super.service.battle.MatchRunnable;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class BattleService {

    @Autowired
    @Getter
    private UserRepository userRepo;

    @Getter
    @Autowired
    private MatchRepository matchRepo;

    @Getter
    private LinkedHashMap<MatchRunnable, Thread> matches = new LinkedHashMap<>();

    private static BattleService instance;

    public BattleService() {

        instance = this;

    }

    public void setup() {

        List<Match> active = matchRepo.findAllByEnded(false);

        for(Match match : active)
            run(match);

    }

    private MatchRunnable run(Match match) {

        if(match == null)
            return null;

        return run(new MatchRunnable(match));

    }

    private MatchRunnable run(MatchRunnable matchRunnable) {

        if(matches.containsKey(matchRunnable) || isRunning(matchRunnable.getMatch()))
            return null;

        Thread battleThread = new Thread(matchRunnable);
        battleThread.start();

        matches.put(matchRunnable, battleThread);
        return matchRunnable;

    }

    public boolean isRunning(Match match) {
        return getRunnable(match) != null;
    }

    public MatchRunnable getRunnable(Match match) {

        for(MatchRunnable runnable : matches.keySet())
            if(runnable.getMatch().getId().toString().equals(match.getId().toString()))
                return runnable;

        return null;

    }

    public Match getCurrent(User user) {

        for(MatchType type : MatchType.values())
            if(type.isVirtual()) {

                Match match = matchRepo.findByMatchTypeAndPlayersInvolvedContainsAndEnded(type, user.getGuid(), false);

                if(match != null)
                    return match;

            }

        return null;

    }

    public MatchRunnable makeInstanceMatch(User user, List<Integer> fleets, int id) {

        Match match = filterInstanceMatch(user, fleets, id);

        if(match == null)
            return null;

        match.setMatchId(getNextMatchId());
        match = matchRepo.save(match);

        MatchRunnable runnable = run(match);
        return runnable;

    }

    private Match filterInstanceMatch(User user, List<Integer> fleets, int id) {

        if(id >= 0 && id <= 29)
            return startInstanceMatch(user, fleets, id);

        return null;

    }

    private Match startInstanceMatch(User user, List<Integer> fleetIds, int id) {

        InstanceData instance = ResourceManager.getInstances().getInstance(id + 1);

        if(instance == null)
            return null;

        if(fleetIds.size() > instance.getPlayerFleets().size())
            return null;

        List<Fleet> fleets = getRealFleets(user, fleetIds);

        if(fleets == null)
            return null;

        Match match = make(instance);

        match.setStartDate(new Date().getTime());
        match.getFleetsInvolved().addAll(fleetIds);
        match.getPlayersInvolved().add(user.getGuid());
        match.getAllyFleets().addAll(createBattleFleets(fleets, instance));

        return match;

    }

    private Match make(InstanceData data) {

        Match match = new Match();

        match.setMatchType(MatchType.INSTANCE_MATCH);

        match.setGalaxyId(-1);
        match.getEnemyFleets().addAll(data.getEnemyFleets());

        match.getTags().add(BattleTag.of("instance", data.getId()));

        return match;

    }

    public List<BattleFleet> createBattleFleets(List<Fleet> realFleets, InstanceData data) {

        List<BattleFleet> fleets = new ArrayList<>();
        List<PlayerFleetMeta> ally = data.getPlayerFleets();

        int index = 0;

        for(Fleet fleet : realFleets) {

            PlayerFleetMeta meta = ally.get(index++);
            BattleFleet battleFleet = new BattleFleet();

            battleFleet.setDefender(true);
            battleFleet.setBodyId(fleet.getBodyId());
            battleFleet.setPosX(meta.getX());
            battleFleet.setPosY(meta.getY());
            battleFleet.setDirection(0);
            battleFleet.setHe3(300);
            battleFleet.setMaxHe3(300);
            battleFleet.setMaxRounds(10);
            battleFleet.setJoinRound(0);
            battleFleet.setGuid(fleet.getGuid());
            battleFleet.setShipTeamId(-1);
            battleFleet.setTeam(BattleFleetTeam.fromShipTeamBody(fleet.getGuid(), fleet.getFleetBody()));
            battleFleet.setTarget(fleet.getTarget());
            battleFleet.setTargetInterval(fleet.getTargetInterval());
            battleFleet.setBattleCommander(fleet.getCommander().createBattleCommander());

            fleets.add(battleFleet);

        }

        return fleets;

    }

    public List<Fleet> getRealFleets(User user, List<Integer> ids) {

        List<Fleet> fleets = new ArrayList<>();

        for(int id : ids) {

            Fleet fleet = PacketService.getFleetRepository().findByShipTeamId(id);

            if(fleet.getGuid() != user.getGuid())
                return null;

            if(fleet == null)
                return null;

            fleets.add(fleet);

        }

        return fleets;

    }

    public int getNextMatchId() {

        Match last = matchRepo.findTopByOrderByIdDesc();

        if(last == null)
            return 1;

        return last.getMatchId() + 1;

    }

    public static int getTarget(String name) {
        switch (name) {
            case "target:maxRange":
                return 1;
            default:
                return 0;
        }
    }

    public static int getTargetInterval(String name) {
        switch (name) {
            case "target:commander":
                return 1;
            case "target:maxAttack":
                return 2;
            case "target:minAttack":
                return 3;
            case "target:maxDurability":
                return 4;
            case "target:minDurability":
                return 5;
            case "target:defBuildings":
                return 6;
            default:
                return 0;
        }
    }

    public static BattleService getInstance() {
        return instance;
    }

}
