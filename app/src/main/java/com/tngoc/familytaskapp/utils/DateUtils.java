package com.tngoc.familytaskapp.utils;

import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateUtils {

    private static final String FORMAT_DATE      = "dd/MM/yyyy";
    private static final String FORMAT_DATETIME  = "dd/MM/yyyy HH:mm";

    public static String formatDate(Timestamp timestamp) {
        if (timestamp == null) return "";
        Date date = timestamp.toDate();
        return new SimpleDateFormat(FORMAT_DATE, Locale.getDefault()).format(date);
    }

    public static String formatDateTime(Timestamp timestamp) {
        if (timestamp == null) return "";
        Date date = timestamp.toDate();
        return new SimpleDateFormat(FORMAT_DATETIME, Locale.getDefault()).format(date);
    }

    public static boolean isOverdue(Timestamp dueDate) {
        if (dueDate == null) return false;
        return dueDate.toDate().before(new Date());
    }
}

