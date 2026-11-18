package com.ai_assistence_service.travelassient.client;

import java.util.function.Supplier;

import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import com.ai_assistence_service.travelassient.exception.TravelServiceException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Runs a remote call and converts every failure into a {@link TravelServiceException}
 * with a readable message, so Spring AI can hand that message back to the LLM.
 */
public final class RemoteCall {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private RemoteCall() {
    }

    public static <T> T execute(String serviceName, Supplier<T> call) {
        try {
            return call.get();
        } catch (RestClientResponseException ex) {
            throw new TravelServiceException(extractMessage(ex));
        } catch (RestClientException | IllegalStateException ex) {
            throw new TravelServiceException(serviceName + " is currently unavailable. Please try again later.");
        }
    }

    private static String extractMessage(RestClientResponseException ex) {
        String body = ex.getResponseBodyAsString();
        try {
            JsonNode node = OBJECT_MAPPER.readTree(body);
            if (node.hasNonNull("message")) {
                return node.get("message").asText();
            }
        } catch (Exception ignored) {
            // body is not JSON, fall through
        }
        return body == null || body.isBlank() ? "Request failed with status " + ex.getStatusCode().value() : body;
    }
}
