package com.wedplan.wedplan.services.paymentMgt;

import java.util.Map;

public record PayHereCheckoutData(String checkoutUrl, Map<String, String> fields) {
}
