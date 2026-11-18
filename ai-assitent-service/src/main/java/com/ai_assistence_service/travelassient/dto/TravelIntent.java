package com.ai_assistence_service.travelassient.dto;

public record TravelIntent(
        IntentType intent,
        String origin,
        String destination,
        String travelDate,
        Integer passengers) {

    public enum IntentType {
        SEARCH_FLIGHT,
        BOOK_FLIGHT,
        VIEW_BOOKING,
        CANCEL_BOOKING,
        CHANGE_SEAT,
        HOTEL,
        WEATHER,
        POLICY_QUESTION,
        OTHER
    }
}
