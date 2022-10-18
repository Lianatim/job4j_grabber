package ru.job4j.grabber.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class HabrCareerDateTimeParser implements DateTimeParser {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-mm-ddТhh:mm:ss");
    @Override
    public LocalDateTime parse(String parse) {
        return LocalDateTime.parse(parse, DATE_TIME_FORMATTER);
    }

}
