package com.go2super.service;

import com.go2super.database.entity.ShipModel;
import com.go2super.database.entity.User;
import com.go2super.database.repository.UserRepository;
import com.go2super.obj.game.ShipTeamNum;
import com.go2super.obj.game.UserLeaderboard;
import com.go2super.obj.model.LoggedGameUser;
import com.google.common.collect.Lists;
import lombok.Getter;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class RankService {

    private static Map<Integer, UserLeaderboard> cachedUser = new HashMap<>();

    private static List<List<UserLeaderboard>> cachedAttackPowerRank = new ArrayList<>();
    private static List<List<UserLeaderboard>> cachedShootdownsRank = new ArrayList<>();

    @Autowired
    @Getter
    private UserRepository userRepo;

    private static RankService instance;

    public RankService() {
        instance = this;
    }

    public void setup() {

        for(User user : userRepo.findAll())
            update(user);

    }

    public void sort() {

        LinkedList<UserLeaderboard> shootdowns = new LinkedList<>(cachedUser.values());
        LinkedList<UserLeaderboard> attack = new LinkedList<>(cachedUser.values());

        Collections.sort(shootdowns, (o1, o2) -> {
            if(o1.getShootdowns() < o2.getShootdowns())
                return 1;
            else if(o1.getShootdowns() > o2.getShootdowns())
                return -1;
            else
                return o1.getGuid() >= o2.getGuid() ? 1 : -1;
        });

        cachedShootdownsRank = Lists.partition(shootdowns, 6);

        Collections.sort(attack, (o1, o2) -> {
            if(o1.getAttackPower() < o2.getAttackPower())
                return 1;
            else if(o1.getAttackPower() > o2.getAttackPower())
                return -1;
            else
                return o1.getGuid() >= o2.getGuid() ? 1 : -1;
        });

        cachedAttackPowerRank = Lists.partition(attack, 6);

    }

    public Pair<Integer, List<UserLeaderboard>> getShootdownsByPageId(int page) {

        if(page >= 0 && cachedShootdownsRank.size() > page)
            return Pair.of(page, cachedShootdownsRank.get(page));

        return Pair.of(0, new ArrayList<>());

    }

    public Pair<Integer, List<UserLeaderboard>> getShootdownsByGuid(int guid) {

        if(!cachedUser.containsKey(guid)) {

            User user = UserService.getInstance().getUserRepository().findByGuid(guid);

            if(user == null)
                return Pair.of(0, cachedShootdownsRank.get(0));

            update(user);
            sort();

        }

        for(int page = 0; page < cachedShootdownsRank.size(); page++) {
            List<UserLeaderboard> users = cachedShootdownsRank.get(page);
            for (UserLeaderboard user : users)
                if (user.getGuid() == guid)
                    return Pair.of(page, users);
        }

        return Pair.of(0, cachedShootdownsRank.get(0));

    }

    public Pair<Integer, List<UserLeaderboard>> getAttackPowerRankByPageId(int page) {

        if(page >= 0 && cachedAttackPowerRank.size() > page)
            return Pair.of(page, cachedAttackPowerRank.get(page));

        return Pair.of(0, new ArrayList<>());

    }

    public Pair<Integer, List<UserLeaderboard>> getAttackPowerRankByGuid(int guid) {

        if(!cachedUser.containsKey(guid)) {

            User user = UserService.getInstance().getUserRepository().findByGuid(guid);

            if(user == null)
                return Pair.of(0, cachedAttackPowerRank.get(0));

            update(user);
            sort();

        }

        for(int page = 0; page < cachedAttackPowerRank.size(); page++) {
            List<UserLeaderboard> users = cachedAttackPowerRank.get(page);
            for (UserLeaderboard user : users)
                if (user.getGuid() == guid)
                    return Pair.of(page, users);
        }

        return Pair.of(0, cachedAttackPowerRank.get(0));

    }

    public int getAttackPowerPages() {
        return cachedAttackPowerRank.size();
    }

    public int getShootdownsPages() {
        return cachedShootdownsRank.size();
    }

    public synchronized void add(User user) {

        UserLeaderboard userLeaderboard = update(user);

        if(!cachedUser.containsKey(userLeaderboard.getGuid()))
            cachedUser.put(userLeaderboard.getGuid(), userLeaderboard);

        if(cachedAttackPowerRank.size() == 0 || cachedShootdownsRank.size() == 0)
            sort();
        else {

            List<UserLeaderboard> attackLeaderboard = cachedAttackPowerRank.get(cachedAttackPowerRank.size() - 1);
            List<UserLeaderboard> shootdownLeaderboard = cachedShootdownsRank.get(cachedShootdownsRank.size() - 1);

            /*if(attackLeaderboard.size() < 6)
                attackLeaderboard.add(userLeaderboard);
            else
                cachedAttackPowerRank.add(Arrays.asList(userLeaderboard));

            if(shootdownLeaderboard.size() < 6)
                shootdownLeaderboard.add(userLeaderboard);
            else
                cachedShootdownsRank.add(Arrays.asList(userLeaderboard));
            */


        }

    }

    public UserLeaderboard update(User user) {

        boolean contains = cachedUser.containsKey(user.getGuid());
        UserLeaderboard leaderboard = contains ? cachedUser.get(user.getGuid()) : new UserLeaderboard();

        leaderboard.setGuid(user.getGuid());
        leaderboard.setShootdowns(user.getStats().getKills());
        leaderboard.setAttackPower(getAttackPower(user));

        if(!contains)
            cachedUser.put(user.getGuid(), leaderboard);

        return leaderboard;

    }

    public int getAttackPower(User user) {

        long attackPower = 0;

        for(ShipTeamNum shipTeamNum : PacketService.getInstance().getAllShipNums(user))
            if(shipTeamNum.getShipModelId() >= -1 && shipTeamNum.getNum() > 0) {

                ShipModel model = PacketService.getShipModel(shipTeamNum.getShipModelId());
                attackPower += model.getMinAttack() * shipTeamNum.getNum();

            }

        return Long.valueOf(attackPower / 1000).intValue();

    }

    public int getAttackPowerRank(User user) {

        int rank = 0;

        for(List<UserLeaderboard> page : new CopyOnWriteArrayList<>(cachedAttackPowerRank))
            for(UserLeaderboard entry : page) {

                if(entry.getGuid() == user.getGuid())
                    return rank;

                rank++;

            }

        return rank;

    }

    public int getShootdownsRank(User user) {

        int rank = 0;

        for(List<UserLeaderboard> page : new CopyOnWriteArrayList<>(cachedShootdownsRank))
            for(UserLeaderboard entry : page) {

                if(entry.getGuid() == user.getGuid())
                    return rank;

                rank++;

            }

        return rank;

    }

    public static void deleteUser(UserLeaderboard userLeaderboard) {

        if(cachedUser.containsKey(userLeaderboard))
            cachedUser.remove(userLeaderboard);

    }

    public static List<UserLeaderboard> getCache() {
        return new ArrayList<>(cachedUser.values());
    }

    public static RankService getInstance() {
        return instance;
    }

}
