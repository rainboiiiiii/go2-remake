package com.go2super.listener;

import com.go2super.database.entity.User;
import com.go2super.obj.game.FriendInfo;
import com.go2super.obj.game.UserBlocked;
import com.go2super.obj.model.LoggedGameUser;
import com.go2super.packet.PacketListener;
import com.go2super.packet.PacketProcessor;
import com.go2super.packet.friend.*;
import com.go2super.service.LoginService;
import com.go2super.service.PacketService;
import com.go2super.service.exception.BadGuidException;
import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public class FriendListener implements PacketListener {

    private List<UserBlocked> blocks = new ArrayList<>();

    @PacketProcessor
    public void onFriendList(RequestFriendListPacket packet) throws BadGuidException {

        LoginService.validate(packet, packet.getGuid());

        User user = PacketService.getUserRepository().findByGuid(packet.getGuid());

        if(user == null)
            return;

        System.out.println(packet.getPageId());

        if(packet.getPageId() < 0)
            return;

        List<User> friends = user.getFriends();
        List<List<User>> pages = Lists.partition(friends, 5);

        if(pages.size() <= packet.getPageId())
            return;

        List<User> page = pages.get(packet.getPageId());
        FriendInfo reference = new FriendInfo();

        List<FriendInfo> infos = getInfos(page);

        while(infos.size() < 5)
            infos.add(reference.trash());

        ResponseFriendListPacket response = new ResponseFriendListPacket();

        response.setDataLen((char) friends.size());
        response.setKind((char) packet.getKind());
        response.setFriendCount((short) friends.size());
        response.setFriendInfos(infos);

        packet.getSmartServer().send(response);

    }

    @PacketProcessor
    public void onAddFriend(RequestAddFriendPacket packet) throws BadGuidException {

        LoginService.validate(packet, packet.getGuid());

        User user = PacketService.getUserRepository().findByGuid(packet.getGuid());

        if(user == null)
            return;

        if(packet.getObjGuid() == user.getGuid())
            return;

        ResponseAddFriendPacket response = new ResponseAddFriendPacket();

        if(user.isFriend(packet.getObjGuid()))
            return;

        updateBlocks();

        if(isBlocked(packet.getGuid(), packet.getObjGuid()))
            return;

        Optional<LoggedGameUser> optional = LoginService.getInstance().getGame(packet.getObjGuid());

        check : if(optional.isPresent()) {

            LoggedGameUser gameUser = optional.get();
            User toUser = gameUser.getUpdatedUser();

            if(toUser == null)
                break check;

            toUser.update();

            ResponseAddFriendAuthPacket popup = new ResponseAddFriendAuthPacket();

            popup.setSrcGuId(user.getGuid());
            popup.setSrcUserId(user.getUserId());
            popup.getSrcName().value(user.getUsername());

            for(int i = 0; i < 2; i++)
                gameUser.getSmartServer().send(popup);

            blocks.add(UserBlocked.builder()
                    .fromGuid(packet.getGuid())
                    .toGuid(packet.getObjGuid())
                    .until(new Date().getTime() + (1000 * 60 * 10))
                    .build());

            response.setErrorCode(0);
            packet.getSmartServer().send(response);
            return;

        }

        response.setErrorCode(1);
        packet.getSmartServer().send(response);

    }

    @PacketProcessor
    public void onFriendPassAuth(RequestFriendPassAuthPacket packet) throws BadGuidException {

        LoginService.validate(packet, packet.getGuid());

        User user = PacketService.getUserRepository().findByGuid(packet.getGuid());

        if(user == null)
            return;

        if(user.isFriend(packet.getFriendGuid()))
            return;

        User friend = PacketService.getUserRepository().findByGuid(packet.getFriendGuid());

        if(friend == null)
            return;

        friend.addFriend(user.getGuid());
        user.addFriend(friend.getGuid());

        user.save();
        friend.save();

        ResponseFriendPassAuthPacket response = new ResponseFriendPassAuthPacket();

        response.setUserId(friend.getUserId());
        response.setFriendGuid(friend.getGuid());
        response.getFriendName().value(friend.getUsername());

        packet.getSmartServer().send(response);

    }

    private boolean isBlocked(int requester, int receiver) {

        for(UserBlocked blocked : blocks)
            if(blocked.getFromGuid() == requester && blocked.getToGuid() == receiver)
                return true;

        return false;

    }

    private List<FriendInfo> getInfos(List<User> list) {

        List<FriendInfo> infos = new ArrayList<>();

        for(User user : list) {

            FriendInfo info = new FriendInfo();

            info.setGuid(user.getGuid());
            info.setLevel(user.getStats().getLevel());
            info.getName().value(user.getUsername());
            info.setUserId(user.getUserId());
            info.setHeadId(1);
            info.setReserve(1);
            info.setStatus(user.isOnline() ? 1 : 0);

            infos.add(info);

        }

        return infos;

    }

    private void updateBlocks() {

        long now = new Date().getTime();
        List<UserBlocked> toRemove = new ArrayList<>();

        for(UserBlocked blocked : blocks)
            if(blocked.getUntil() < now)
                toRemove.add(blocked);

        blocks.removeAll(toRemove);

    }

}
