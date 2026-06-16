package com.eshop.marketing.domain.sql;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * JPA entity representing a marketing campaign stored in SQL Server.
 *
 * <p>The {@code [From]} and {@code [To]} column names are SQL Server reserved
 * keywords and must be quoted in the column mapping.
 */
@Entity
@Table(name = "Campaigns")
@Data
@NoArgsConstructor
public class Campaign {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "Description", length = 500)
    private String description;

    @Column(name = "CampaignTypeId", nullable = false)
    private int campaignTypeId;

    @Column(name = "CampaignType", length = 100)
    private String campaignType;

    @Column(name = "[From]", nullable = false)
    private Instant from;

    @Column(name = "[To]", nullable = false)
    private Instant to;

    @Column(name = "PictureUrl", length = 500)
    private String pictureUrl;

    @Column(name = "DetailsUrl", length = 500)
    private String detailsUrl;

    @Column(name = "ActionText", length = 255)
    private String actionText;
}
