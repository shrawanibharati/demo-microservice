package com.example.util;

import java.text.SimpleDateFormat;
import java.util.Date;

public interface CommonUtils {

    public static String formatDate(String pattern, Date requestedDate) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
        String dateYearMonthDay = simpleDateFormat.format(requestedDate);
        return dateYearMonthDay;
    }
}
