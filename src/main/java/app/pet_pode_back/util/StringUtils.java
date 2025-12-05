package app.pet_pode_back.util;

import java.text.Normalizer;
import java.util.regex.Pattern;

public class StringUtils {
    private static final Pattern NON_ASCII = Pattern.compile("[^\\p{ASCII}]");

    public static String normalize(String input) {
        if (input == null) return null;
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);
        return NON_ASCII.matcher(normalized).replaceAll("").toLowerCase();
    }
}
