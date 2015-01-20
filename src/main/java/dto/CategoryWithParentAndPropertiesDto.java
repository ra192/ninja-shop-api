package dto;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import model.Category;
import model.Property;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yakov_000 on 25.06.2014.
 */
public class CategoryWithParentAndPropertiesDto extends CategoryDto {

    String parent;

    @JacksonXmlElementWrapper(localName = "properties")
    @JacksonXmlProperty(localName = "property")
    List<String>properties;

    public CategoryWithParentAndPropertiesDto() {
    }

    public CategoryWithParentAndPropertiesDto(Category category) {
        super(category);

        if(category.getParent()!=null)
            parent = category.getParent().getName();

        properties=new ArrayList<>();
        for(Property property:category.getProperties())
            properties.add(property.getName());
    }

    public List<String> getProperties() {
        return properties;
    }

    public void setProperties(List<String> properties) {
        this.properties = properties;
    }

    public String getParent() {
        return parent;
    }

    public void setParent(String parent) {
        this.parent = parent;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("CategoryWithParentAndPropertiesDto{");
        sb.append("parent='").append(parent).append('\'');
        sb.append(super.toString());
        sb.append(", properties=").append(properties);
        sb.append('}');
        return sb.toString();
    }
}
