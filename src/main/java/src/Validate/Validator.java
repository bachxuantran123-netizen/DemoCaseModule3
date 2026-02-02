package src.Validate;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.regex.Pattern;

public class Validator {
    private static final String REGEX_ACCOUNT_CODE = "^\\d{9}$";
    private static final String REGEX_CITIZEN_ID = "^(\\d{9}|\\d{12})$";

    public static boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

    public static boolean isValidAccountCode(String code) {
        return code != null && Pattern.matches(REGEX_ACCOUNT_CODE, code);
    }

    public static boolean isValidCitizenId(String id) {
        return id != null && Pattern.matches(REGEX_CITIZEN_ID, id);
    }

    public static boolean isPositiveNumber(String strNum) {
        if (isEmpty(strNum)) return false;
        try {
            double value = Double.parseDouble(strNum.replace(",", ""));
            return value > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}