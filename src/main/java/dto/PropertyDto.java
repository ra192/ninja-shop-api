package dto;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import model.Property;
import model.PropertyValue;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by yakov_000 on 24.06.2014.
 */

public class PropertyDto {
    @NotNull
    String name;
    @NotNull
    String displayName;

    @JacksonXmlElementWrapper(localName = "propertyValues")
    @JacksonXmlProperty(localName = "propertyValue")
    @Valid
    List<PropertyValueDto> propertyValues;

    public PropertyDto() {

    }

    public PropertyDto(Property property) {
        name=property.getName();
        displayName=property.getDisplayName();
        propertyValues=new ArrayList<>();
    }

    public List<PropertyValueDto> getPropertyValues() {
        return propertyValues;
    }

    public void setPropertyValues(List<PropertyValueDto> propertyValues) {
        this.propertyValues = propertyValues;
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
        final StringBuffer sb = new StringBuffer("PropertyDto{");
        sb.append("name='").append(name).append('\'');
        sb.append(", displayName='").append(displayName).append('\'');
        sb.append(", propertyValues=").append(propertyValues);
        sb.append('}');
        return sb.toString();
    }
}
