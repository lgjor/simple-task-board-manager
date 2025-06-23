package org.desviante.util;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.OffsetDateTime;

public class DateTimeConversion {
    private static final DateTimeFormatter RFC_3339_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX");

    public static String formatToRFC3339(LocalDate date, LocalTime time) {
        ZonedDateTime zdt = ZonedDateTime.of(date, time, ZoneId.systemDefault());
        return zdt.format(RFC_3339_FORMATTER);
    }

    public static OffsetDateTime toOffsetDateTime(LocalDate date, LocalTime time) {
        ZonedDateTime zdt = ZonedDateTime.of(date, time, ZoneId.systemDefault());
        return zdt.toOffsetDateTime();
    }
}