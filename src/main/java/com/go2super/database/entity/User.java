package com.go2super.database.entity;

import com.go2super.database.entity.sub.*;
import com.go2super.obj.model.LoggedGameUser;
import com.go2super.obj.type.BonusType;
import com.go2super.packet.props.ResponseTimeQueuePacket;
import com.go2super.service.*;
import com.go2super.socket.util.DateUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import javax.persistence.Column;
import javax.persistence.Id;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Document(collection = "game_users")
@Builder
@Data
@AllArgsConstructor
public class User {

    @Id
    private ObjectId id;

    @Column(unique = true) private int guid;
    @Column(unique = true) private long userId;
    @Column(unique = true) private String username;

    private String typeStart;
    private int ground;

    private int gMapId;
    private int consortiaId;
    private int consortiaJob;
    private int consortiaUnionLevel;
    private int gameServerId;
    private int card1;
    private int cardCredit;
    private int card2;
    private int card3;
    private int cardUnion;
    private int chargeFlag;
    private int shipSpeedCredit;
    private int lotteryStatus;
    private int consortiaThrow;
    private int consortiaUnion;
    private int consortiaShop;
    private int ectypeNum;
    private int tollGate;
    private short year;
    private int month;
    private int day;
    private int noviceGuide;
    private int warScore;

    private Date lastRecruit;


    @Field(name = "game_stats") private UserStats stats;

    @Field(name = "game_ships") private UserShips ships;
    @Field(name = "game_resources") private UserResources resources;
    @Field(name = "game_buildings") private UserBuildings buildings;
    @Field(name = "game_resource_storage") private UserStorage storage;
    @Field(name = "game_user_inventory") private UserInventory inventory;
    @Field(name = "game_user_friends") private List<Integer> friends;

    public void update() {
        UserService.getInstance().updateStats(this);
    }

    public void save() {
        UserService.getInstance().getUserRepository().save(this);
    }

    public Optional<LoggedGameUser> getLoggedGameUser() {
        return LoginService.getInstance().getGame(this);
    }

    public boolean isOnline() {
        return getLoggedGameUser().isPresent();
    }

    public void addFriend(int guid) {
        if(!getFriendsIds().contains(guid))
            getFriendsIds().add(guid);
    }

    public void removeFriend(int guid) {
        if(getFriendsIds().contains(guid))
            getFriendsIds().remove(guid);
    }

    public boolean isFriend(int guid) {
        return getFriendsIds().contains(guid);
    }

    public List<Integer> getFriendsIds() {
        if(friends == null)
            friends = new ArrayList<>();
        return friends;
    }

    public List<User> getFriends() {

        List<User> friends = new ArrayList<>();

        for(int friendId : getFriendsIds()) {

            User friend = UserService.getInstance().getUserRepository().findByGuid(friendId);

            if(friend == null)
                continue;

            friends.add(friend);

        }

        return friends;

    }

    public int getViewFlag(int requester) {
        if(requester == guid || isFriend(requester))
            return 1;
        return 0;
    }

    public int getRanking() {
        return RankService.getInstance().getAttackPower(this);
    }

    public int getCityLevel() {
        return 0;
    }

    public int getSpaceStationLevel() {
        return 0;
    }

    public int getCurrentLeague() {
        return 0;
    }

    public int getCurrentInstance() {
        return 0;
    }

    public int getStarFace() {

        List<BonusType> types = stats.getAllBonuses();

        if(types.contains(BonusType.PLANET_APPEARANCE))
            if(types.contains(BonusType.LUXURIOUS_GOLD_RESOURCE_PRODUCTION))
                return 10;
            else if(types.contains(BonusType.METALLIC_METAL_RESOURCE_PRODUCTION))
                return 11;
            else if(types.contains(BonusType.GASEOUS_HE3_RESOURCE_PRODUCTION))
                return 12;
            else if(types.contains(BonusType.ORDINARY_PLANET))
                return 13;
            else if(types.contains(BonusType.CHRISTMAS_RESOURCE_PRODUCTION))
                return 14;
            else if(types.contains(BonusType.GF_RESOURCE_PRODUCTION))
                return 15;
            else if(types.contains(BonusType.HALLOWEEN_RESOURCE_PRODUCTION))
                return 16;

        return 9;

    }

    public int getStarType() {
        int level = getStats().getLevel();
        return level <= 10 ? 4 : (level <= 20 ? 5 : 6);
    }

    public int getAttackPowerRank() {
        return RankService.getInstance().getAttackPowerRank(this);
    }

    public int getShootdownsRank() {
        return RankService.getInstance().getShootdownsRank(this);
    }

    public int getNextRecruit() {

        long next = lastRecruit.getTime() + 5400000;

        if(DateUtil.now().getTime() > next)
            return 0;

        return Long.valueOf(TimeUnit.MILLISECONDS.toSeconds(next)).intValue();

    }

    public Commander getCommanderBySkill(int skillId) {
        return CommanderService.getInstance().getCommanderRepository().findBySkillAndUserId(skillId, getPlanet().getUserId());
    }

    public Commander getCommander(int commanderId) {
        return CommanderService.getInstance().getCommanderRepository().findByCommanderIdAndUserId(commanderId, getPlanet().getUserId());
    }

    public int getSpins() {

        if(resources.getLastSpin() != null) {
            if (DateUtil.currentDay(resources.getLastSpin()))
                return resources.getFreeSpins();
        } else {
            resources.setLastSpin(DateUtil.date(0));
        }

        boolean mvp = stats.hasBonus(BonusType.MVP_DAILY_DRAWS_BONUS);
        resources.setFreeSpins(mvp ? 3 : 1);

        return resources.getFreeSpins();

    }

    public ResponseTimeQueuePacket getQueuesAsPacket() {
        return UserService.getInstance().getUserQueues(this);
    }

    public int totalShips() {
        return countFleetShips() + ships.countStoredShips();
    }

    public int countFleetShips() {
        int ships = 0;
        for(Fleet fleet : getFleets())
            ships += fleet.ships();
        return ships;
    }

    public List<Fleet> getFleets() {
        return PacketService.getFleetRepository().findAllByGuid(this.getGuid());
    }

    public List<Commander> getCommanders() {
        return CommanderService.getInstance().getCommanders(this);
    }

    public UserPlanet getPlanet() {
        return GalaxyService.getInstance().getUserPlanet(this);
    }

}
