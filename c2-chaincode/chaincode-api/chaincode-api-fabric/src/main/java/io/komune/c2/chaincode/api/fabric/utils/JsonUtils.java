package io.komune.c2.chaincode.api.fabric.utils;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class JsonUtils {

    private static final JsonMapper mapper;

    static {
        mapper = JsonMapper.builder()
                .changeDefaultVisibility(vc -> vc
                        .withFieldVisibility(JsonAutoDetect.Visibility.ANY))
                .build();
    }

    public static String toJson(Object obj) {
        return mapper.writeValueAsString(obj);
    }

    public static <T> T toObject(URL url, Class<T> clazz) throws IOException {
        try (InputStream is = url.openStream()) {
            return mapper.readValue(is, clazz);
        }
    }

}
