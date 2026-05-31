package com.go2super.resources;

import com.go2super.resources.json.*;
import com.go2super.resources.serialization.PropsJsonDeserializer;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import lombok.SneakyThrows;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

@Component
public class ResourceManager {

    private static ResourceManager instance;

    private PropsJson propsJson;
    private LevelsJson levelsJson;
    private BuildsJson buildsJson;

    private ShipPartJson shipPartJson;
    private ShipBodyJson shipBodyJson;

    private GalaxyMapJson galaxyMapJson;
    private CommandersJson commandersJson;

    private CorpsPirateJson corpsPirateJson;
    private CorpsShopJson corpsShopJson;
    private InstancesJson instancesJson;
    private ShipModelsJson shipModelsJson;

    private LotteryJson lotteryJson;

    public ResourceManager() {

        instance = this;

        this.propsJson = getJson("props.json", PropsJson.class, new PropsJsonDeserializer());
        this.levelsJson = getJson("levels.json", LevelsJson.class);
        this.galaxyMapJson = getJson("galaxyMap.json", GalaxyMapJson.class);
        this.buildsJson = getJson("builds.json", BuildsJson.class);
        this.commandersJson = getJson("commanders.json", CommandersJson.class);

        this.shipPartJson = getJson("shipPart.json", ShipPartJson.class);
        this.shipBodyJson = getJson("shipBody.json", ShipBodyJson.class);

        this.corpsPirateJson = getJson("corpsPirate.json", CorpsPirateJson.class);
        this.corpsShopJson = getJson("corpsShop.json", CorpsShopJson.class);
        this.instancesJson = getJson("instances.json", InstancesJson.class);
        this.shipModelsJson = getJson("shipModels.json", ShipModelsJson.class);

        this.lotteryJson = getJson("lottery.json", LotteryJson.class);

    }

    @SneakyThrows
    private <T> T getJson(String jsonFile, Class<T> clazz, JsonDeserializer<T> deserializer) {
        return new GsonBuilder().registerTypeAdapter(clazz, deserializer).create().fromJson(getReader(jsonFile), clazz);
    }

    @SneakyThrows
    private <T> T getJson(String jsonFile, Class<T> clazz) {
        return new Gson().fromJson(getReader(jsonFile), clazz);
    }

    @SneakyThrows
    private Reader getReader(String file) {
        return new InputStreamReader(getInputStream(file), "UTF-8");
    }

    @SneakyThrows
    private InputStream getInputStream(String file) {
        return new ClassPathResource("data/" + file).getInputStream();
    }

    public static PropsJson getProps() {
        return instance.propsJson;
    }

    public static BuildsJson getBuilds() {
        return instance.buildsJson;
    }

    public static LotteryJson getLottery() {
        return instance.lotteryJson;
    }

    public static CorpsPirateJson getCorpsPirate() {
        return instance.corpsPirateJson;
    }

    public static LevelsJson getLevels() {
        return instance.levelsJson;
    }

    public static CorpsShopJson getCorpsShop() {
        return instance.corpsShopJson;
    }

    public static ShipPartJson getShipParts() {
        return instance.shipPartJson;
    }

    public static ShipBodyJson getShipBodies() {
        return instance.shipBodyJson;
    }

    public static ShipModelsJson getShipModels() {
        return instance.shipModelsJson;
    }

    public static CommandersJson getCommanders() {
        return instance.commandersJson;
    }

    public static InstancesJson getInstances() {
        return instance.instancesJson;
    }

    public static GalaxyMapJson getGalaxyMaps() {
        return instance.galaxyMapJson;
    }

    public static ResourceManager getInstance() {
        return instance;
    }

}