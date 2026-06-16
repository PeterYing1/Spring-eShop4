package com.eshop.location.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * MongoDB document tracking which {@link Location} a user is currently at.
 *
 * <p>Mirrors the .NET {@code UserLocation} model stored in the
 * {@code UserLocation} MongoDB collection.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "UserLocation")
public class UserLocation {

    @Id
    private String id;

    private String userId;

    private Location location;
}
