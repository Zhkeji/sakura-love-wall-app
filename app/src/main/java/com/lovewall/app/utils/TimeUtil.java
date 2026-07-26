package com.lovewall.app.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class TimeUtil {
    public static String getTimeAgo(String dateStr) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date date = sdf.parse(dateStr);
            if (date == null) return dateStr;
            long diff = (System.currentTimeMillis() - date.getTime()) / 1000;
            if (diff < 60) return "刚刚";
            if (diff < 3600) return (diff / 60) + "分钟前";
            if (diff < 86400) return (diff / 3600) + "小时前";
            if (diff < 2592000) return (diff / 86400) + "天前";
            if (diff < 31536000) return (diff / 2592000) + "个月前";
            return (diff / 31536000) + "年前";
        } catch (Exception e) {
            return dateStr;
        }
    }
}
