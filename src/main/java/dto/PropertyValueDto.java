package dto;

import model.PropertyValue;

import javax.validation.constraints.NotNull;

/**
 * Created by yakov_000 on 24.06.2014.
 */
public class PropertyValueDto {
    @NotNull
    String name;
    @NotNull
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

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("{");
        sb.append("name='").append(name).append('\'');
        sb.append(", displayName='").append(displayName).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
