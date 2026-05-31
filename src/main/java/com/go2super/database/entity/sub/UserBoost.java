package com.go2super.database.entity.sub;

import com.go2super.database.entity.GameBoost;
import com.go2super.service.ResourcesService;
import com.go2super.socket.util.DateUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.Date;
import java.util.Optional;

@Builder
@AllArgsConstructor
@Data
public class UserBoost {

    private String gameBoostId;
    private Date until;

    public GameBoost boost() {
        return getGameBoost().get();
    }

    public Optional<GameBoost> getGameBoost() {
        return ResourcesService.getInstance().getBoostRepository().findById(gameBoostId);
    }

    public int getSeconds() {
        return DateUtil.remains(until).intValue();
    }

    public void addSeconds(int seconds) {
        setUntil(DateUtil.offset(until, seconds));
    }

}
