package com.example.sqlitedemo.util;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtil {
    public static String getNowDateTime(String formatStr) {
    	String format = formatStr;
    	if (format==null || format.length()<=0) {
    		format = "yyyy-MM-dd HH:mm:ss";
    	}
        SimpleDateFormat s_format = new SimpleDateFormat(format);
        return s_format.format(new Date());
    }

    public static String getNowTime() {
        SimpleDateFormat s_format = new SimpleDateFormat("HH:mm:ss");
        return s_format.format(new Date());
    }
}
