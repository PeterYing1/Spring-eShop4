package com.eshop.marketing.api;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request body for the {@code PUT /api/v1/campaigns/user/location} endpoint.
 */
@Data
@NoArgsConstructor
public class LocationUpdate {

    private String locationId;
}
