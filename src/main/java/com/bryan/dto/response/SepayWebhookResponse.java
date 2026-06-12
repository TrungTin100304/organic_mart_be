package com.bryan.dto.response;

public record SepayWebhookResponse(
    boolean success,
    String message
) {
    public static SepayWebhookResponse ok() {
        return new SepayWebhookResponse(true, null);
    }

    public static SepayWebhookResponse ok(String message) {
        return new SepayWebhookResponse(true, message);
    }

    public static SepayWebhookResponse error(String message) {
        return new SepayWebhookResponse(false, message);
    }
}
