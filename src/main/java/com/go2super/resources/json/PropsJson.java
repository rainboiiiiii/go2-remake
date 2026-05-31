package com.go2super.resources.json;

import com.go2super.resources.data.PropData;
import lombok.Data;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Data
@ToString
public class PropsJson {

    private List<PropData> props;

    private List<PropData> gems = new ArrayList<>();
    private List<PropData> commanders = new ArrayList<>();
    private List<PropData> inSell = new ArrayList<>();

    public PropData getGemData(int id) {
        for(PropData data : getGems())
            if(data.getId() == id)
                return data;
        return null;
    }

    public PropData getData(int id) {
        for(PropData data : props)
            if(data.getId() == id)
                return data;
        return null;
    }

    public List<PropData> getGems() {
        if(gems.size() == 0)
            for(PropData data : props)
                if(data.getType().equals("gem"))
                    gems.add(data);
        return gems;
    }

    public List<PropData> getCommanders() {
        if(commanders.size() == 0)
            for(PropData data : props)
                if(data.getType().equals("commander"))
                    commanders.add(data);
        return commanders;
    }

    public List<PropData> getInSell() {
        if(inSell.size() == 0)
            for(PropData data : props)
                if(data.hasMallData())
                    inSell.add(data);
        return inSell;
    }

}
