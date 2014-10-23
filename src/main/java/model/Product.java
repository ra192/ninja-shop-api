package model;

import javax.persistence.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by yakov_000 on 05.06.2014.
 */
@Entity
public class Product {
    private Long id;
    private String code;
    private String displayName;
    private Double price;
    private Category category;
    private String imageUrl;
    private String description;
    private Set<PropertyValue>propertyValues;
    private Float rating;
//    voteUserIds:List[IdentityId]=List.empty

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    @ManyToOne
    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
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

    @ManyToMany
    public Set<PropertyValue> getPropertyValues() {
        return propertyValues;
    }

    public void setPropertyValues(Set<PropertyValue> propertyValues) {
        this.propertyValues = propertyValues;
    }

    public Float getRating() {
        return rating;
    }

    public void setRating(Float rating) {
        this.rating = rating;
    }

    public Map<String,Object>toMap() {

        final Map<String, Object> result = new HashMap<>();

        result.put("code",code);
        result.put("displayName",displayName);
        result.put("description",description);
        result.put("imageUrl",imageUrl);
        result.put("price",price);
        result.put("rating",rating);

        return result;
    }
}
