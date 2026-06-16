package com.eshop.location.domain;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Value object representing a geographic coordinate pair sent by the client
 * when updating a user's current position.
 */
@Data
@AllArgsConstructor
public class GeoLocation {

    private double latitude;

    private double longitude;
}
