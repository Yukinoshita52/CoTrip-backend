package com.trip.model.dto;

import lombok.Data;

/**
 * ClassName: TripDTO
 * Package: com.trip.model.dto
 * Description:
 *
 * @Author YukinoshitaYukino
 * @Create 2025/11/21 20:08
 * @Version 1.0
 */
@Data
public class TripDTO {
    private Long tripId;
    private String name;
    private String description;
}
