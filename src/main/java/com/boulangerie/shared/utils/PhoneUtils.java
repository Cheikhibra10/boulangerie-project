package com.boulangerie.shared.utils;

import com.boulangerie.shared.exception.BadRequestException;

import java.util.regex.Pattern;

/**
 * Utility for Senegalese phone numbers only (national 9 digits).
 *
 * <p>Mobile: 70–79 + 7 digits
 * <p>Landline: 3X + 7 digits
 */
public final class PhoneUtils {

    private static final Pattern SENEGAL_PHONE =
            Pattern.compile("^7(?:0|1|2|5|6|7|8|9)\\d{7}$|^3\\d{8}$");
    private PhoneUtils() {}

    /**
     * Returns 9 national digits, or null if blank / invalid.
     * Accepts common formats: 77 123 45 67, 0771234567, +221..., 00221...
     */
    public static String normalize(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }

        String d = raw.replaceAll("[^0-9]", "");

        // Remove country code if present
        if (d.startsWith("00221")) {
            d = d.substring(5);
        } else if (d.startsWith("221") && d.length() >= 12) {
            d = d.substring(3);
        } else if (d.startsWith("0") && d.length() == 10) {
            d = d.substring(1);
        }

        return d.length() == 9 ? d : null;
    }

    /**
     * Returns the number in E.164 format (+221XXXXXXXXX).
     * Returns null if blank. Throws if invalid.
     */
    public static String isValidFormat(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }

        String national = normalize(raw);
        if (national == null || !SENEGAL_PHONE.matcher(national).matches()) {
            throw new BadRequestException(
                    "Numéro de téléphone invalide: " + raw);
        }
        return national;
    }
}