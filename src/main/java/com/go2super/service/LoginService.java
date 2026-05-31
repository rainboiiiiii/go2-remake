package com.go2super.service;

import com.go2super.database.entity.*;
import com.go2super.database.entity.type.UserRank;
import com.go2super.database.entity.type.AccountStatus;
import com.go2super.database.repository.AccountRepository;
import com.go2super.database.repository.CommanderRepository;
import com.go2super.dto.AccountDTO;
import com.go2super.dto.response.BasicResponse;
import com.go2super.dto.response.WebUserDTO;
import com.go2super.obj.model.LoggedGameUser;
import com.go2super.obj.model.LoggedSessionAccount;
import com.go2super.obj.model.LoggedSessionUser;
import com.go2super.packet.Packet;
import com.go2super.packet.login.PlayerLoginTogPacket;
import com.go2super.service.exception.BadGuidException;
import com.go2super.socket.util.Crypto;
import com.go2super.socket.util.MathUtil;
import lombok.SneakyThrows;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.net.InetAddress;
import java.util.*;

@Service
public class LoginService {

    private static LoginService loginService;

    private List<LoggedGameUser> gameUsers = new ArrayList<>();
    private List<LoggedSessionUser> sessionUsers = new ArrayList<>();
    private List<LoggedSessionAccount> sessionAccounts = new ArrayList<>();

    @Autowired
    CommanderRepository commanderRepository;

    @Autowired
    private AccountRepository accountRepository;

    public LoginService() {
        loginService = this;
    }

    public void addGameUser(LoggedGameUser loggedUser) {
        gameUsers.add(loggedUser);
    }
    public List<LoggedGameUser> getGameUsers() {
        return gameUsers;
    }

    public void addSessionUser(LoggedSessionUser loggedUser) {
        sessionUsers.add(loggedUser);
    }
    public List<LoggedSessionUser> getSessionUsers() { return sessionUsers; }
    public Optional<LoggedSessionUser> getSession(long userId) {
        return sessionUsers.stream().filter(user -> user.getUser().getPlanet().getUserId() == userId).findAny();
    }

    public Optional<LoggedSessionAccount> getSessionAccount(String token) {
        return sessionAccounts.stream().filter(account -> account.getToken().equals(token)).findAny();
    }

    @SneakyThrows
    public void disconnectGame(LoggedSessionUser user) {

        for(LoggedGameUser loggedGameUser : LoginService.getInstance().getGameUsers())
            if(loggedGameUser.getUserId() == user.getUser().getPlanet().getUserId()) {
                loggedGameUser.getSmartServer().close();
                gameUsers.remove(loggedGameUser);
                break;
            }
    }

    @SneakyThrows
    public void disconnectUser(LoggedSessionUser user) {
        sessionUsers.remove(user);
    }

    @SneakyThrows
    public void disconnectWeb(LoggedSessionAccount account) {
        sessionAccounts.remove(account);
    }

    public InetAddress getAddress(int guid) {

        for(LoggedGameUser user : gameUsers)
            if(user.getGuid() == guid)
                return user.getAddress();

        return null;

    }



    public Optional<LoggedGameUser> getGame(long userId) {
        return gameUsers.stream().filter(user -> user.getUserId() == userId).findAny();
    }

    public Optional<LoggedGameUser> getGame(int guid) {
        return gameUsers.stream().filter(user -> user.getGuid() == guid).findAny();
    }

    public Optional<LoggedGameUser> getGame(User user) {
        return gameUsers.stream().filter(guser -> guser.getGuid() == user.getGuid()).findAny();
    }

    public Optional<LoggedGameUser> getGameByFormula(int guid) {
        return gameUsers.stream().filter(user -> user.getGuid() == MathUtil.toRealGuid(guid)).findAny();
    }

    public LoggedGameUser login(LoggedSessionUser sessionUser, PlayerLoginTogPacket packet) {

        LoggedGameUser gameUser = LoggedGameUser.builder()
                .address(packet.getSmartServer().getRemoteAddress())
                .guid(sessionUser.getUser().getGuid())
                .userId(sessionUser.getUser().getPlanet().getUserId())
                .loggedSessionUser(sessionUser)
                .smartServer(packet.getSmartServer()).build();

        gameUsers.add(gameUser);
        return gameUser;

    }

    public BasicResponse login(AccountDTO dto) {

        Optional<Account> accountOptional = accountRepository.findByEmail(dto.getEmail())
                .or(() -> accountRepository.findByUsername(dto.getUsername()));

        if(!accountOptional.isPresent())
            return BasicResponse
                    .builder()
                    .code(HttpStatus.NOT_ACCEPTABLE.value())
                    .message("ACCOUNT_NOT_FOUND")
                    .build();

        Account account = accountOptional.get();

        if(Crypto.decrypt(account.getPassword()).equals(dto.getPassword())) {

            String token = RandomStringUtils.randomAlphanumeric(40);

            Optional<LoggedSessionAccount> oldLoggedAccount =
                    sessionAccounts
                            .stream()
                            .filter(session -> session.getAccount().getEmail().equals(account.getEmail()))
                            .findAny();

            if(oldLoggedAccount.isPresent()){

                oldLoggedAccount
                        .get()
                        .getAccount()
                        .getUserIds()
                        .stream()
                        .forEach(userId -> {

                            Optional<LoggedSessionUser> loggedSessionUser =
                                    sessionUsers
                                    .stream()
                                    .filter(session -> session.getUser().getUserId() == userId)
                                    .findAny();

                            if(loggedSessionUser.isPresent()){
                                disconnectGame(loggedSessionUser.get());
                                disconnectUser(loggedSessionUser.get());
                            }

                        });

                disconnectWeb(oldLoggedAccount.get());
            }


            sessionAccounts.add(
                    LoggedSessionAccount.builder()
                    .account(account)
                    .token(token)
                    .build()
            );

            WebUserDTO webUserDTO = WebUserDTO
                    .builder()
                    .vip(account.getVip())
                    .email(account.getEmail())
                    .username(account.getUsername())
                    .rank(account.getUserRank())
                    .token(token)
                    .build();

            return BasicResponse
                    .builder()
                    .code(HttpStatus.OK.value())
                    .message("LOGIN_COMPLETED")
                    .data(webUserDTO)
                    .build();
        }

        return BasicResponse
                .builder()
                .code(HttpStatus.NOT_ACCEPTABLE.value())
                .message("INVALID_CREDENTIALS")
                .build();
    }

    public BasicResponse register(AccountDTO dto){

        Optional<Account> account =
                accountRepository.findByEmail(dto.getEmail())
                .or(() -> accountRepository.findByUsername(dto.getUsername()));


        if(account.isPresent())

            if(dto.getEmail().equals(account.get().getEmail()))
                return BasicResponse
                        .builder()
                        .code(HttpStatus.NOT_ACCEPTABLE.value())
                        .message("EMAIL_TAKEN")
                        .build();

            else if(dto.getUsername().equals(account.get().getUsername()))
                return BasicResponse
                        .builder()
                        .code(HttpStatus.NOT_ACCEPTABLE.value())
                        .message("USERNAME_TAKEN")
                        .build();

        Account newAccount =  Account
                .builder()
                .email(dto.getEmail())
                .username(dto.getUsername())
                .password(Crypto.encrypt(dto.getPassword()))
                .accountStatus(AccountStatus.REGISTER)
                .userRank(UserRank.USER)
                .registerDate(now())
                .vip(0)
                .build();

        accountRepository.save(newAccount);

        return BasicResponse
                .builder()
                .code(HttpStatus.OK.value())
                .message("REGISTER_ACCOUNT_COMPLETED")
                .build();
    }


    public static void validate(Packet packet, int guid) throws BadGuidException {

        Optional<LoggedGameUser> loggedGameUser = getInstance().getGame(guid);

        if(!loggedGameUser.isPresent())
            throw new BadGuidException();

        LoggedGameUser gameUser = loggedGameUser.get();

        if(!gameUser.getSmartServer().getConnectionId().equals(packet.getSmartServer().getConnectionId()))
            throw new BadGuidException();

        return;

    }

    public List<LoggedGameUser> getViewers(long userId) {

        List<LoggedGameUser> viewers = new ArrayList<>();

        for(LoggedGameUser gameUser : getGameUsers())
            if(gameUser.getViewing() == userId)
                viewers.add(gameUser);

        return viewers;

    }

    public static Date now() {
        return new Date();
    }

    public static LoginService getInstance() {
        return loginService;
    }

}
