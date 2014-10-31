package dto;

import model.PropertyValue;

/**
 * Created by yakov_000 on 24.06.2014.
 */
public class PropertyValueDto {
    String name;
    String displayName;

    public PropertyValueDto() {

    }

    public PropertyValueDto(PropertyValue propertyValue) {
        name=propertyValue.getName();
        displayName=propertyValue.getDisplayName();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
}
