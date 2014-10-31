package dto;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import model.Category;
import model.Product;
import model.PropertyValue;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yakov_000 on 25.06.2014.
 */
public class ProductDto {
    String code;
    String displayName;
    Double price;
    String category;
    String imageUrl;
    String description;

    @JacksonXmlElementWrapper(localName = "propertyValues")
    @JacksonXmlProperty(localName = "propertyValue")
    List<String> propertyValues;

    public ProductDto() {

    }

    public ProductDto(Product product) {
        code=product.getCode();
        displayName=product.getDisplayName();
        price=product.getPrice();
        imageUrl=product.getImageUrl();
        category=product.getCategory().getName();

        propertyValues=new ArrayList<>();
        for(PropertyValue propertyValue:product.getPropertyValues()) {
            propertyValues.add(propertyValue.getName());
        }
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getPropertyValues() {
        return propertyValues;
    }

    public void setPropertyValues(List<String> propertyValues) {
        this.propertyValues = propertyValues;
    }
}
