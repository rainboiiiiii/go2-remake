package com.go2super.resources.json;

import lombok.Data;
import lombok.ToString;

import java.util.Map;

@Data
@ToString
public class LocalizationJson {

    private String locale;
    private Map<String, Map<String, String>> strings;

    public String getLocale() {
        return locale;
    }

    public boolean has(String group, String key) {
        return strings.containsKey(group) && strings.get(group).containsKey(key);
    }

    public String get(String group, String key) {
        try {
            return strings.get(group).get(key);
        } catch(Exception e) {
            return "Undefined: " + key;
        }
    }

    public String get(String fullKey) {
        String[] composition = fullKey.split(":");
        return get(composition[0], composition[1]);
    }

}
