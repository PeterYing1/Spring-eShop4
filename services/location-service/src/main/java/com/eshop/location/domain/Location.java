package com.eshop.location.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

/**
 * MongoDB document representing a geographical location (city, area, region).
 *
 * <p>The {@code subcity} list allows hierarchical location trees where a location
 * can contain nested sub-locations.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "Locations")
public class Location {

    @Id
    private String id;

    private String userId;

    private String locationId;

    private double latitude;

    private double longitude;

    private String description;

    private boolean active;

    private String code;

    private int order;

    private List<Location> subcity;
}
