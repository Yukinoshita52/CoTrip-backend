package com.trip.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class DetailInfo {
    @JsonProperty("classified_poi_tag")
    private String classifiedPoiTag;

    private String price;

    @JsonProperty("shop_hours")
    private String shopHours;

    @JsonProperty("overall_rating")
    private String overallRating;
}

