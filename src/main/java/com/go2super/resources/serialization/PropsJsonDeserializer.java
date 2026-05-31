package com.go2super.resources.serialization;

import com.go2super.resources.data.PropData;
import com.go2super.resources.data.props.*;
import com.go2super.resources.json.PropsJson;
import com.google.gson.*;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class PropsJsonDeserializer implements JsonDeserializer<PropsJson> {

    @Override
    public PropsJson deserialize(JsonElement jsonElement, Type clazzType, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {

        PropsJson result = new PropsJson();
        JsonArray props = jsonElement.getAsJsonObject().get("props").getAsJsonArray();

        result.setProps(new ArrayList<>());

        for(JsonElement element : props) {

            PropData obj = new PropData();
            JsonObject prop = element.getAsJsonObject();

            int id = prop.get("id").getAsInt();
            String name = prop.get("name").getAsString();
            String type = prop.get("type").getAsString();

            PropMetaData data = null;
            PropMallData[] mall = new PropMallData[0];

            switch(type) {

                case "basic":
                    break;

                case "commander":
                    data = new Gson().fromJson(prop.get("data"), PropCommanderData.class);
                    break;

                case "scroll":
                    data = new Gson().fromJson(prop.get("data"), PropScrollData.class);
                    break;

                case "buff":
                    data = new Gson().fromJson(prop.get("data"), PropBuffData.class);
                    break;

                case "gem":
                    data = new Gson().fromJson(prop.get("data"), PropGemData.class);
                    break;

            }

            if(prop.has("mall"))
                mall = new Gson().fromJson(prop.get("mall"), PropMallData[].class);

            obj.setId(id);
            obj.setName(name);
            obj.setType(type);
            obj.setData(data);
            obj.setMall(mall);

            result.getProps().add(obj);

        }

        return result;

    }

}
