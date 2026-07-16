package com.bryan.dto.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SepayWebhookRequestJsonTest {

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Test
    void deserializesSepayTransactionDateWithSpaceSeparator() throws Exception {
        String json = """
            {
              "gateway": "TPBank",
              "transactionDate": "2026-06-12 15:05:03",
              "accountNumber": "00003981468",
              "subAccount": null,
              "code": null,
              "content": "OM355B83874FD6",
              "transferType": "in",
              "transferAmount": 2000,
              "referenceCode": "277V602261631487",
              "accumulated": 4838,
              "id": 62982235
            }
            """;

        SepayWebhookRequest request = objectMapper.readValue(json, SepayWebhookRequest.class);

        assertEquals(2026, request.transactionDate().getYear());
        assertEquals(15, request.transactionDate().getHour());
    }
}
