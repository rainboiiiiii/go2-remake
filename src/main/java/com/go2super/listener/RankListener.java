package com.go2super.listener;

import com.go2super.database.entity.User;
import com.go2super.obj.game.RankUserInfo;
import com.go2super.obj.game.UserLeaderboard;
import com.go2super.packet.PacketListener;
import com.go2super.packet.PacketProcessor;
import com.go2super.packet.instance.RequestEctypePacket;
import com.go2super.packet.rank.RequestRankCentPacket;
import com.go2super.packet.rank.RequestRankKillTotalPacket;
import com.go2super.packet.rank.ResponseRankCentPacket;
import com.go2super.packet.rank.ResponseRankKillTotalPacket;
import com.go2super.service.LoginService;
import com.go2super.service.PacketService;
import com.go2super.service.RankService;
import com.go2super.service.UserService;
import com.go2super.service.exception.BadGuidException;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;

public class RankListener implements PacketListener {

    @PacketProcessor
    public void onRankCent(RequestRankCentPacket packet) throws BadGuidException {

        LoginService.validate(packet, packet.getGuid());

        User user = PacketService.getUserRepository().findByGuid(packet.getGuid());

        if(user == null)
            return;

        Pair<Integer, List<UserLeaderboard>> page = null;

        if(packet.getPageId() >= 0)
            page = RankService.getInstance().getAttackPowerRankByPageId(packet.getPageId());
        else if(packet.getGuid() >= 0)
            page = RankService.getInstance().getAttackPowerRankByGuid(packet.getGuid());
        else if(packet.getObjGuid() >= 0)
            page = RankService.getInstance().getAttackPowerRankByGuid(packet.getGuid());

        if(page == null)
            return;

        List<RankUserInfo> ranking = getRanking(page.getValue(), page.getKey(), 6);
        RankUserInfo reference = new RankUserInfo();

        int size = ranking.size();

        while(ranking.size() < 6)
            ranking.add(reference.trash());

        ResponseRankCentPacket response = new ResponseRankCentPacket();

        response.setPageId(page.getKey());
        response.setMaxPageId(RankService.getInstance().getAttackPowerPages());

        response.setDataLen(size);
        response.setUsers(ranking);

        packet.getSmartServer().send(response);

    }

    @PacketProcessor
    public void onRankKillTotal(RequestRankKillTotalPacket packet) throws BadGuidException {

        LoginService.validate(packet, packet.getGuid());

        User user = PacketService.getUserRepository().findByGuid(packet.getGuid());

        if(user == null)
            return;

        Pair<Integer, List<UserLeaderboard>> page = null;

        if(packet.getPageId() >= 0)
            page = RankService.getInstance().getShootdownsByPageId(packet.getPageId());
        else if(packet.getGuid() >= 0)
            page = RankService.getInstance().getShootdownsByGuid(packet.getGuid());
        else if(packet.getObjGuid() >= 0)
            page = RankService.getInstance().getShootdownsByGuid(packet.getGuid());

        if(page == null)
            return;

        List<RankUserInfo> ranking = getRanking(page.getValue(), page.getKey(), 6);
        RankUserInfo reference = new RankUserInfo();

        int size = ranking.size();

        while(ranking.size() < 6)
            ranking.add(reference.trash());

        ResponseRankKillTotalPacket response = new ResponseRankKillTotalPacket();

        response.setPageId(page.getKey());
        response.setMaxPageId(RankService.getInstance().getAttackPowerPages());

        response.setDataLen(size);
        response.setUsers(ranking);

        packet.getSmartServer().send(response);

    }

    private List<RankUserInfo> getRanking(List<UserLeaderboard> leaderboards, int page, int max) {

        List<RankUserInfo> ranking = new ArrayList<>();

        int pos = page <= 0 ? 0 : page * max;

        for(UserLeaderboard userLeaderboard : leaderboards) {

            User ranked = UserService.getInstance().getUserRepository().findByGuid(userLeaderboard.getGuid());

            if(ranked == null)
                continue;

            RankUserInfo rankUserInfo = new RankUserInfo();

            rankUserInfo.setRankId(pos++);
            rankUserInfo.setLevel(ranked.getStats().getLevel());
            rankUserInfo.setConsortiaId(-1);
            rankUserInfo.setUserId(ranked.getUserId());
            rankUserInfo.setGuid(ranked.getGuid());
            rankUserInfo.setLevel(ranked.getStats().getLevel());
            rankUserInfo.getName().setValue(ranked.getUsername());
            rankUserInfo.setAssault(userLeaderboard.getAttackPower());
            rankUserInfo.setKillTotal(userLeaderboard.getShootdowns());

            ranking.add(rankUserInfo);

        }

        return ranking;

    }

}
