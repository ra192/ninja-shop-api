package dto;

import model.PropertyValue;

/**
 * Created by yakov_000 on 26.06.2014.
 */

public class PropertyValueWithCountDto extends PropertyValueDto {
    public Long count;

    public PropertyValueWithCountDto(PropertyValue propertyValue, Long count) {
        super(propertyValue);
        this.count=count;
    }
}
