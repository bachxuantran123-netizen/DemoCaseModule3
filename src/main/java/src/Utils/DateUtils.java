package src.Utils;

import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

public class DateUtils {
    public static Date convertToSqlDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return new Date(System.currentTimeMillis());
        }
        try {
            return Date.valueOf(dateStr);
        } catch (IllegalArgumentException e) {}
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        try {
            java.util.Date utilDate = sdf.parse(dateStr);
            return new Date(utilDate.getTime());
        } catch (ParseException e) {
            System.out.println("Lỗi ngày tháng tự động lấy ngày hiện tại...");
            return new Date(System.currentTimeMillis());
        }
    }
    public static boolean isFutureDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return false;
        }
        try {
            LocalDate inputDate = LocalDate.parse(dateStr);
            LocalDate today = LocalDate.now();
            return inputDate.isAfter(today);
        } catch (DateTimeParseException e) {
            return false;
        }
    }
}