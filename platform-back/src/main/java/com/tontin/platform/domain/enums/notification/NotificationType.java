package com.tontin.platform.domain.enums.notification;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Category of a notification. API uses lowercase with underscores (e.g. payment_due).
 */
public enum NotificationType {

    PAYMENT_DUE("payment_due"),
    PAYMENT_RECEIVED("payment_received"),
    PAYOUT_READY("payout_ready"),
    DAR_INVITATION("dar_invitation"),
    MEMBER_JOINED("member_joined"),
    MEMBER_LEFT("member_left"),
    TOUR_COMPLETED("tour_completed"),
    REMINDER("reminder"),
    SYSTEM("system"),
    TRUST_SCORE("trust_score"),
    MESSAGE("message"),
    WARNING("warning"),
    INFORMATION("information"),
    ALERT("alert");

    private final String apiValue;

    NotificationType(String apiValue) {
        this.apiValue = apiValue;
    }

    @JsonValue
    public String getApiValue() {
        return apiValue;
    }

    @JsonCreator
    public static NotificationType fromApiValue(String value) {
        if (value == null) return null;
        for (NotificationType t : values()) {
            if (t.apiValue.equalsIgnoreCase(value)) return t;
        }
        return INFORMATION;
    }
}
