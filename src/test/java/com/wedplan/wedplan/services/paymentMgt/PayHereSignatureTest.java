package com.wedplan.wedplan.services.paymentMgt;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PayHereSignatureTest {
    @Test
    void createsPayHereCheckoutHashFromTwoDecimalAmount() {
        assertEquals("FEB654CE967DDABB7A194CAC69F17370",
                PayHereSignature.checkoutHash(
                        "12345678", "ORDER-1", new BigDecimal("10.00"), "LKR", "secret"));
    }

    @Test
    void validatesNotificationSignatureAndRejectsTampering() {
        assertTrue(PayHereSignature.verifyNotification(
                "12345678", "ORDER-1", "10.00", "LKR", "2",
                "9E7DF4A1174ECF487B04A38835BCB2D2", "secret"));
        assertFalse(PayHereSignature.verifyNotification(
                "12345678", "ORDER-1", "10.01", "LKR", "2",
                "9E7DF4A1174ECF487B04A38835BCB2D2", "secret"));
    }
}
