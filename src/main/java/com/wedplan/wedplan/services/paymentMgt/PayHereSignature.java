package com.wedplan.wedplan.services.paymentMgt;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HexFormat;

public final class PayHereSignature {
    private static final SecureRandom RANDOM = new SecureRandom();

    private PayHereSignature() {
    }

    public static String checkoutHash(String merchantId, String orderId, BigDecimal amount,
                                      String currency, String merchantSecret) {
        String formattedAmount = amount.setScale(2, RoundingMode.UNNECESSARY).toPlainString();
        return md5Upper(merchantId + orderId + formattedAmount + currency + md5Upper(merchantSecret));
    }

    public static boolean verifyNotification(
            String merchantId,
            String orderId,
            String amount,
            String currency,
            String statusCode,
            String receivedSignature,
            String merchantSecret) {
        if (receivedSignature == null || receivedSignature.isBlank()) {
            return false;
        }
        String expected = md5Upper(merchantId + orderId + amount + currency + statusCode + md5Upper(merchantSecret));
        return MessageDigest.isEqual(
                expected.getBytes(StandardCharsets.US_ASCII),
                receivedSignature.trim().toUpperCase().getBytes(StandardCharsets.US_ASCII)
        );
    }

    private static String md5Upper(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            return HexFormat.of().withUpperCase().formatHex(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("The required MD5 digest algorithm is unavailable.", exception);
        }
    }
}
