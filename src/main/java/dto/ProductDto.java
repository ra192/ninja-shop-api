package dto;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import model.Category;
import model.Product;
import model.PropertyValue;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by yakov_000 on 25.06.2014.
 */
public class ProductDto {
    @NotNull
    String code;
    @NotNull
    String displayName;
    @NotNull
    Double price;
    @NotNull
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

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("ProductDto{");
        sb.append("code='").append(code).append('\'');
        sb.append(", displayName='").append(displayName).append('\'');
        sb.append(", price=").append(price);
        sb.append(", category='").append(category).append('\'');
        sb.append(", imageUrl='").append(imageUrl).append('\'');
        sb.append(", description='").append(description).append('\'');
        sb.append(", propertyValues=").append(propertyValues);
        sb.append('}');
        return sb.toString();
    }
}
