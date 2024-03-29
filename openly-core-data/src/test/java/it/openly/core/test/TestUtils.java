package it.openly.core.test;

import lombok.SneakyThrows;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class TestUtils {
    @SneakyThrows
    public static Date dt(String date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.parse(date);
    }

    public static Map<String, Object> map(Object... args) {
        Map<String, Object> result = new HashMap<>();
        for(int i = 0; i < args.length; i += 2) {
            result.put((String)args[i], args[i + 1]);
        }
        return result;
    }


}
