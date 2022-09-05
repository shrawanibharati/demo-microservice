package com.example.util;

import java.text.SimpleDateFormat;
import java.util.Date;

public interface CommonUtils {

    public static final String HOUSE_WITH_ID = "House with id : ";
    public static final String NOT_FOUND = " not found!";

    public static final String YYYYMMDD = "yyyy-MM-dd";
    public static final String YYYYMM = "yyyy-MM";

    public static String formatDate(String pattern, Date requestedDate) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
        return simpleDateFormat.format(requestedDate);
    }
}
