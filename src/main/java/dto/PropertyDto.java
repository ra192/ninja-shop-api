package dto;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import model.Property;
import model.PropertyValue;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yakov_000 on 24.06.2014.
 */

public class PropertyDto {
    String name;
    String displayName;

    @JacksonXmlElementWrapper(localName = "propertyValues")
    @JacksonXmlProperty(localName = "propertyValue")
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
}
