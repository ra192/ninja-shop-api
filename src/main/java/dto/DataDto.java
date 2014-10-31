package dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.List;

/**
 * Created by yakov_000 on 24.06.2014.
 */

@JacksonXmlRootElement(localName = "data")
@JsonIgnoreProperties(ignoreUnknown = true)
public class DataDto {

    @JacksonXmlElementWrapper(localName = "properties")
    @JacksonXmlProperty(localName = "property")
    List<PropertyDto> properties;

    @JacksonXmlElementWrapper(localName = "categories")
    @JacksonXmlProperty(localName = "category")
    List<CategoryWithParentAndPropertiesDto> categories;

    @JacksonXmlElementWrapper(localName = "products")
    @JacksonXmlProperty(localName = "product")
    List<ProductDto>products;

    public List<ProductDto> getProducts() {
        return products;
    }

    public void setProducts(List<ProductDto> products) {
        this.products = products;
    }

    public List<CategoryWithParentAndPropertiesDto> getCategories() {
        return categories;
    }

    public void setCategories(List<CategoryWithParentAndPropertiesDto> categories) {
        this.categories = categories;
    }

    public List<PropertyDto> getProperties() {
        return properties;
    }

    public void setProperties(List<PropertyDto> properties) {
        this.properties = properties;
    }
}
