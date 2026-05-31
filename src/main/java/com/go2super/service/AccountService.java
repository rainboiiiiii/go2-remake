package com.go2super.service;

import com.go2super.database.entity.Account;
import com.go2super.database.entity.Commander;
import com.go2super.database.entity.Planet;
import com.go2super.database.entity.User;
import com.go2super.database.entity.sub.*;
import com.go2super.database.repository.AccountRepository;
import com.go2super.database.repository.CommanderRepository;
import com.go2super.database.repository.PlanetRepository;
import com.go2super.database.repository.UserRepository;
import com.go2super.dto.CreateUserDTO;
import com.go2super.dto.response.BasicResponse;
import com.go2super.dto.response.PlayUserDTO;
import com.go2super.dto.response.UserDTO;
import com.go2super.obj.model.LoggedSessionAccount;
import com.go2super.obj.model.LoggedSessionUser;
import com.go2super.obj.utility.GalaxyTile;
import com.go2super.service.jobs.user.RankJob;
import com.go2super.socket.util.DateUtil;
import com.go2super.socket.util.RandomUtil;
import org.apache.commons.lang3.RandomStringUtils;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class AccountService {

    private static int MAX_PLANET = 8388607;

    private static int MAX_USER = 3;

    String usernamePattern = "^[\\w\\d\\S]{1,32}$";
    Pattern pattern = Pattern.compile(usernamePattern);

    private static AccountService accountService;

    @Autowired
    private LoginService loginService;

    @Autowired
    CommanderRepository commanderRepository;

    @Autowired
    private PlanetRepository planetRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Value("${application.public.base-url:http://localhost:9090}")
    private String publicBaseUrl;

    @Value("${application.game.websocket-path:/ws/game}")
    private String websocketPath;

    public AccountService() {
        accountService = this;
    }

    public BasicResponse listOfUser(HttpServletRequest request){
        Optional<LoggedSessionAccount> sessionAccount = loginService.getSessionAccount(request.getHeader("Authorization"));

        if(sessionAccount.isEmpty()) {
            return BasicResponse
                    .builder()
                    .code(HttpStatus.UNAUTHORIZED.value())
                    .message("UNAUTHORIZED")
                    .build();
        }

        Account account = sessionAccount.get().getAccount();
        List<Long> userIds = account.getUserIds() != null ? account.getUserIds() : Collections.emptyList();

        List<UserDTO> users = userIds
                .stream()
                .map(x -> {

                    User user = userRepository.findByUserId(x);
                    UserDTO userDTO = UserDTO
                            .builder()
                            .username(user.getUsername())
                            .typeStart(String.format("%d-%d", user.getStarFace(), user.getStarType()))
                            .ground(user.getGround())
                            .userId(user.getUserId())
                            .resources(user.getResources())
                            .build();

                    return userDTO;
                })
                .collect(Collectors.toList());

        return BasicResponse
                .builder()
                .code(HttpStatus.OK.value())
                .message("OK")
                .data(users)
                .build();
    }

    public BasicResponse play(long userId, HttpServletRequest request){

        Optional<LoggedSessionAccount> sessionAccount = loginService.getSessionAccount(request.getHeader("Authorization"));

        if(sessionAccount.isEmpty())
            return BasicResponse
                    .builder()
                    .code(HttpStatus.UNAUTHORIZED.value())
                    .message("UNAUTHORIZED")
                    .build();

        Account account = sessionAccount.get().getAccount();
        List<Long> userIds = account.getUserIds() != null ? account.getUserIds() : Collections.emptyList();
        Optional<Long> id = userIds.stream().filter(x -> x == userId).findAny();

        if (id.isEmpty())
            return BasicResponse
                    .builder()
                    .code(HttpStatus.NOT_FOUND.value())
                    .message("NOT_FOUND")
                    .build();

        Optional<LoggedSessionUser> oldLoggedUser = loginService.getSessionUsers()
                .stream()
                .filter(session -> session.getUser().getUserId() == userId)
                .findAny();

        if(oldLoggedUser.isPresent()){
            loginService.disconnectGame(oldLoggedUser.get());
            loginService.disconnectUser(oldLoggedUser.get());
        }

        User user = userRepository.findByUserId(id.get());

        String sessionKey = RandomStringUtils.randomAlphanumeric(25);

        LoggedSessionUser session = LoggedSessionUser
                .builder()
                .user(user)
                .sessionKey(sessionKey)
                .build();

        loginService.addSessionUser(session);

        String gameUrl = buildGameWebSocketUrl();

        PlayUserDTO userDTO = PlayUserDTO
                .builder()
                .gameUrl(gameUrl)
                .url(gameUrl)
                .sessionKey(sessionKey)
                .userId(user.getUserId())
                .build();

        return BasicResponse
                .builder()
                .code(HttpStatus.OK.value())
                .message("OK")
                .data(userDTO)
                .build();
    }

    public BasicResponse createUser(CreateUserDTO dto, HttpServletRequest request){
        Optional<LoggedSessionAccount> sessionAccount = loginService.getSessionAccount(request.getHeader("Authorization"));

        if(sessionAccount.isEmpty())
            return BasicResponse
                    .builder()
                    .code(HttpStatus.UNAUTHORIZED.value())
                    .message("UNAUTHORIZED")
                    .build();

        Matcher matcher = pattern.matcher(dto.getUsername());

        if(!matcher.find())
            return BasicResponse
                    .builder()
                    .code(HttpStatus.UNAUTHORIZED.value())
                    .message("NOT_VALID_USERNAME")
                    .build();

        Optional<User> user = userRepository.findByUsername(dto.getUsername());

        if(user.isPresent())
            return BasicResponse
                    .builder()
                    .code(HttpStatus.OK.value())
                    .message("USERNAME_TAKEN")
                    .build();

        Account account = sessionAccount.get().getAccount();
        if (account.getUserIds() == null) {
            account.setUserIds(new ArrayList<>());
        }

        if(account.getUserIds().size() >= MAX_USER)
            return BasicResponse
                    .builder()
                    .code(HttpStatus.OK.value())
                    .message("MAX_USER")
                    .build();

        long userId = getNextUserId();
        account.getUserIds().add(userId);

        User newUser = User.builder()

                .id(ObjectId.get())
                .guid(getNextGuid())
                .userId(userId)

                .username(dto.getUsername())
                .typeStart(dto.getTypeStar())
                .ground(Math.max(0, Math.min(2, dto.getGround())))

                .gameServerId(' ')
                .card1(0)
                .card2(0)
                .card3(0)
                .cardCredit(0)
                .cardUnion(0)
                .chargeFlag(0)
                .shipSpeedCredit(0)
                .lotteryStatus(0)
                .consortiaId(0)
                .consortiaJob(0)
                .consortiaUnionLevel(0)
                .consortiaShop(0)
                .consortiaThrow(0)
                .consortiaUnion(0)
                .day(0)
                .year((short) 0)
                .month(0)
                .ectypeNum(0)
                .gMapId(0)
                .noviceGuide(0)
                .tollGate(0)
                .lastRecruit(DateUtil.now(-1000000))

                .build();

        UserPlanet userPlanet = createNewUserPlanet(newUser, userId);

        while(userRepository.findByGuid(newUser.getGuid()) != null)
            newUser.setGuid(RandomUtil.getRandomInt(MAX_PLANET));

        newUser.setShips(createNewUserShips());
        newUser.setBuildings(createNewUserBuildings());
        newUser.setStorage(createNewUserStorage());
        newUser.setInventory(createNewUserInventory());
        newUser.setStats(createNewUserStats());
        newUser.setResources(createNewUserResources());

        accountRepository.save(account);
        userRepository.save(newUser);
        planetRepository.save(userPlanet);

        generateCommanders(newUser);
        JobService.getOfflineJob(RankJob.class).synchronizedAdd(newUser);

        return BasicResponse
                .builder()
                .code(HttpStatus.OK.value())
                .message("CREATE_COMPLETED")
                .build();
    }

    public void generateCommanders(User user) {

        for(int i = -1; i < 9; i++)
            commanderRepository.save(CommanderService.getInstance().basic(i, user.getPlanet().getUserId()));

    }

    public UserStats createNewUserStats() {
        return UserStats.builder()
                .exp(0)
                .sp(10)
                .maxSp(10)
                .build();
    }

    public UserShips createNewUserShips() {
        return UserShips.builder()
                .ships(new ArrayList<>())
                .factory(new ArrayList<>())
                .build();
    }

    public UserResources createNewUserResources() {
        return UserResources.builder()
                .badge(9000000)
                .honor(9000000)
                .corsairs(9000000)
                .coupons(9000000)
                .mallPoints(9000000)
                .vouchers(90000000)
                .gold(90000000)
                .metal(90000000)
                .he3(90000000)
                .freeSpins(1)
                .build();
    }

    public UserStorage createNewUserStorage() {

        return UserStorage.builder()
                .he3(0)
                .metal(0)
                .gold(0)
                .maxHe3(100000)
                .maxMetal(100000)
                .maxGold(100000)
                .goldProduction(0)
                .metalProduction(0)
                .he3Production(0)
                .lastProductionCalculus(new Date()).build();

    }

    public UserBuildings createNewUserBuildings() {

        UserBuildings buildings = UserBuildings.builder()
                .build()
                .addBuilding(1, 0, 693, 886)
                .addBuilding(2, 0, 914, 962)
                .addBuilding(3, 0, 1136, 982)
                .addBuilding(0, 0, 1082, 802)
                .addBuilding(13, 0, 12, 12)
                .addBuilding(4, 0, 831, 774);

        return buildings;

    }

    public UserInventory createNewUserInventory() {

        UserInventory inventory = UserInventory.builder()
                .maximumStacks(200)
                .stackPrice(1000)
                .propList(new ArrayList<>()).build()
                .debugAddProp(921, 9999, 0)
                .debugAddProp(1338, 9999, 0)
                .debugAddProp(1273, 9999, 0)
                .debugAddProp(1283, 9999, 0);

        // for(GameBoost boost : UserService.getInstance().getBoostRepository().findAll())
        //    inventory.debugAddProp(boost.getPropId(), 100, 0);

        // for(PropData prop : ResourceManager.getProps().getCommanders())
        //    inventory.debugAddProp(prop.getId(), 1, 0);

        return inventory;

    }

    public UserPlanet createNewUserPlanet(User user, long userId) {
        GalaxyTile randomTile = GalaxyService.getInstance().randomAvailablePosition();
        return new UserPlanet(user.getId().toString(), userId, randomTile);
    }

    public int getNextCommanderId() {

        Commander last = commanderRepository.findTopByOrderByIdDesc();

        if(last == null)
            return 1;

        return last.getCommanderId() + 1;

    }

    public int getNextGuid() {

        User last = userRepository.findTopByOrderByIdDesc();

        if(last == null)
            return 1;

        return last.getGuid() + 1;

    }

    public long getNextUserId() {

        Planet last = planetRepository.findTopByOrderByIdDesc();

        if(last == null)
            return 1;

        return last.getUserId() + 1;

    }

    public static Date now() {
        return new Date();
    }

    public static AccountService getInstance() {
        return accountService;
    }

    public LoginService getLoginService() {
        return loginService;
    }

    private String buildGameWebSocketUrl() {
        String normalizedBase = publicBaseUrl.endsWith("/")
                ? publicBaseUrl.substring(0, publicBaseUrl.length() - 1)
                : publicBaseUrl;

        if (normalizedBase.startsWith("https://")) {
            return "wss://" + normalizedBase.substring("https://".length()) + websocketPath;
        }

        if (normalizedBase.startsWith("http://")) {
            return "ws://" + normalizedBase.substring("http://".length()) + websocketPath;
        }

        return "ws://" + normalizedBase + websocketPath;
    }

}
