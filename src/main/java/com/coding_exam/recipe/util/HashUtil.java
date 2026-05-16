package com.coding_exam.recipe.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

public final class HashUtil {

    private HashUtil() {}

    public static String getBase64Sha256(String input) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hashBytes = digest.digest(input.getBytes(StandardCharsets.UTF_8));
        
        return Base64.getUrlEncoder().withoutPadding().encodeToString(hashBytes);
    }
}