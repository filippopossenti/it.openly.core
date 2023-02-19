package it.openly.core.utils;

import lombok.SneakyThrows;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Shortcuts {

    private Shortcuts() {
        // no instantiating this class
    }

    /**
     * Converts a string representing a date in yyyy-MM-dd format into a java.util.Date
     * @param date The string representation of a date
     * @return The resulting Date object
     */
    @SneakyThrows
    public static Date dt(String date) {
        return dt(date, "yyyy-MM-dd");
    }

    /**
     * Converts a string representing a date in an arbitrary format into a java.util.Date
     * @param date The string representation of a date
     * @param format The format
     * @return The resulting Date object
     */
    @SneakyThrows
    public static Date dt(String date, String format) {
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.parse(date);
    }
}
