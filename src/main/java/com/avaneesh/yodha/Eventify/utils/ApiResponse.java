package com.avaneesh.yodha.Eventify.utils;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * A standardized, immutable wrapper for API responses.
 * Using a Java record provides immutability, conciseness, and automatically generated
 * constructors, getters, equals, hashCode, and toString methods.
 *
 * @param success A boolean indicating if the request was successful.
 * @param message A descriptive message about the outcome of the request.
 * @param data    The payload of the response. Can be null.
 * @param <T>     The type of the data payload.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(
        boolean success,
        String message,
        T data
) {
}
