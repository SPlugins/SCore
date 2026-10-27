package com.ssomar.score.utils.time;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TimeConverter {

    @SuppressWarnings("deprecation")
    public static String longToText(long time) {
        Timestamp ts = new Timestamp(time);
        String nanos = ts.getNanos() + "";
        nanos = nanos.substring(0, 1);

        String timer = "";
        if (ts.getHours() - 1 > 0) timer = timer.replaceAll("%H%", (ts.getHours() - 1) + "");

        timer = timer.replaceAll("%M%", ts.getMinutes() + "");
        timer = timer.replaceAll("%S%", ts.getSeconds() + "");
        timer = timer.replaceAll("%N%", nanos);

        return timer;
    }
}
