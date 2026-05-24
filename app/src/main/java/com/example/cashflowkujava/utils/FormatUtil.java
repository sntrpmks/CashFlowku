package com.example.cashflowkujava.utils;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class FormatUtil {

    private static final Locale LOCALE_ID = new Locale("id", "ID");

    public static String formatRupiah(double amount) {
        NumberFormat formatter = NumberFormat.getCurrencyInstance(LOCALE_ID);
        // Clean default currency code representation
        String formatted = formatter.format(amount);
        // Replace symbol to clean Rp
        if (formatted.startsWith("Rp")) {
            formatted = formatted.replace("Rp", "Rp ").replaceAll(",00", "");
        }
        return formatted;
    }

    public static String formatDateToIndonesian(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return "-";
        }
        try {
            if (dateStr.length() > 10) {
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US);
                Date date = inputFormat.parse(dateStr);
                SimpleDateFormat outputFormat = new SimpleDateFormat("dd MMMM yyyy, HH:mm", LOCALE_ID);
                return outputFormat.format(date);
            } else {
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
                Date date = inputFormat.parse(dateStr);
                SimpleDateFormat outputFormat = new SimpleDateFormat("dd MMMM yyyy", LOCALE_ID);
                return outputFormat.format(date);
            }
        } catch (ParseException e) {
            return dateStr;
        }
    }

    public static String getTodayDateString() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US);
        return sdf.format(new Date());
    }

    public static String getTodayJustDateString() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        return sdf.format(new Date());
    }

    public static String getFormattedDate(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        return sdf.format(date);
    }
}
