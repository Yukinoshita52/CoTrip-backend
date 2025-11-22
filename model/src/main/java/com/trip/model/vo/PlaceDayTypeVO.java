package com.trip.model.vo;

import com.trip.model.entity.Place;
import lombok.Data;

import java.util.List;

/**
 * ClassName: PlaceDayVO
 * Package: com.trip.model.vo
 * Description:
 *
 * @Author YukinoshitaYukino
 * @Create 2025/11/22 19:08
 * @Version 1.0
 */
@Data
public class PlaceDayTypeVO {
    private Integer day;
    private List<PlaceType> places;

    @Data
    public static class PlaceType {
        private Integer placeId;
        private String name;
        private Float lat;
        private Float lng;
        private String address;
        private String type;
    }
}
