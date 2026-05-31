package com.go2super.listener;

import com.go2super.database.entity.User;
import com.go2super.obj.game.Prop;
import com.go2super.packet.PacketListener;
import com.go2super.packet.PacketProcessor;
import com.go2super.packet.mall.RequestBuyGoodsPacket;
import com.go2super.packet.mall.ResponseBuyGoodsPacket;
import com.go2super.resources.ResourceManager;
import com.go2super.resources.data.PropData;
import com.go2super.resources.data.props.PropMallData;
import com.go2super.service.LoginService;
import com.go2super.service.UserService;
import com.go2super.service.exception.BadGuidException;

import java.util.List;
import java.util.Optional;

public class MallListener implements PacketListener {

    @PacketProcessor
    public void onBuyGoods(RequestBuyGoodsPacket packet) throws BadGuidException {

        LoginService.validate(packet, packet.getGuid());

        if(packet.getNum() <= 0)
            return;

        User user = UserService.getInstance().getUserRepository().findByGuid(packet.getGuid());

        if(user == null)
            return;

        List<PropData> props = ResourceManager.getProps().getInSell();
        Optional<PropData> optionalGood = props.stream().filter(cache -> cache.getId() == packet.getPropsId()).findAny();

        if(!optionalGood.isPresent())
            return;

        PropData good = optionalGood.get();

        for(PropMallData mall : good.getMall())
            if (mall.getCurrency() == packet.getCurrency()) {

                int price = packet.getNum() * mall.getValue();
                int quantity = packet.getNum() * mall.getAmount();

                if(quantity > 9999)
                    return;

                boolean canBuy = false;
                int lockFlag = mall.isBound() ? 1 : 0;

                Prop prop = user.getInventory().getProp(good.getId(), lockFlag);

                if(prop != null && ((mall.isBound() ? prop.getPropLockNum() : prop.getPropNum()) + quantity) <= 9999) {
                    canBuy = true;
                } else if(prop == null && user.getInventory().countStacks(0) + 1 <= user.getInventory().getMaximumStacks()) {
                    canBuy = true;
                }

                if(!canBuy)
                    return;

                switch(mall.getCurrency()) {

                    case 0: // MP

                        if(user.getResources().getMallPoints() >= price)
                            user.getResources().setMallPoints(user.getResources().getMallPoints() - price);
                        else
                            return;

                        break;

                    case 1: // VOUCHERS

                        if(user.getResources().getVouchers() >= price)
                            user.getResources().setVouchers(user.getResources().getVouchers() - price);
                        else
                            return;

                        break;

                    case 2: // BADGE

                        if(user.getResources().getBadge() >= price)
                            user.getResources().setBadge(user.getResources().getBadge() - price);
                        else
                            return;

                        break;

                    case 3: // HONOR

                        if(user.getResources().getHonor() >= price)
                            user.getResources().setHonor(user.getResources().getHonor() - price);
                        else
                            return;

                        break;

                    case 4: // CHAMPS

                        if(user.getResources().getChampionPoints() >= price)
                            user.getResources().setChampionPoints(user.getResources().getChampionPoints() - price);
                        else
                            return;

                        break;

                    default:
                        return;

                }

                if(!user.getInventory().addProp(good.getId(), quantity, 0, mall.isBound()))
                    return;

                ResponseBuyGoodsPacket response = UserService.getInstance().getBuyGoodsPacket(good, mall, lockFlag, quantity, price);

                packet.getSmartServer().send(response);
                user.save();
                return;

            }

        return;

    }

}
