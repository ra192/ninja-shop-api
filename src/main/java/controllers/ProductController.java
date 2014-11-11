package controllers;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import dao.CategoryDao;
import dao.ProductDao;
import dao.PropertyDao;
import dto.ProductDto;
import dto.PropertyDto;
import dto.PropertyValueDto;
import filters.CorsFilter;
import model.Category;
import model.Product;
import model.Property;
import model.PropertyValue;
import ninja.FilterWith;
import ninja.Result;
import ninja.Results;
import ninja.jpa.UnitOfWork;
import ninja.params.PathParam;

import java.util.*;

@Singleton
@FilterWith(CorsFilter.class)
public class ProductController {

    public static class PropertiesFilter {
        public List<String> propertyValues;
    }

    public static class PropertyResultItem extends PropertyDto implements Comparable<PropertyResultItem> {

        public Boolean isAdditional;

        public PropertyResultItem(Property property, Boolean isAdditional) {
            super(property);
            this.isAdditional = isAdditional;
        }

        @Override
        public int compareTo(PropertyResultItem o) {
            return getDisplayName().compareTo(o.getDisplayName());
        }
    }

    public static class PropertyValueResultItem extends PropertyValueDto {
        public Long count;

        public PropertyValueResultItem(PropertyValue propertyValue, Long count) {
            super(propertyValue);
            this.count = count;
        }
    }

    @Inject
    ProductDao productDao;

    @Inject
    CategoryDao categoryDao;

    @Inject
    PropertyDao propertyDao;

    @UnitOfWork
    public Result products(@PathParam("categoryName") String categoryName) {

        Category category = categoryDao.getByName(categoryName);

        if (category == null) {
            return Results.json().render("error", "category with specified name was not found");
        }

        final List<ProductDto> result = new ArrayList<>();

        for (Product product : productDao.listByCategory(category)) {
            result.add(new ProductDto(product));
        }

        return Results.json().render("data", result);
    }

    @UnitOfWork
    public Result product(@PathParam("id") Long id) {

        Product product = productDao.get(id);

        if (product != null)
            return Results.json().render(new ProductDto(product));
        else
            return Results.json().render("error", "product with specified id was not found");
    }

    @UnitOfWork
    public Result properties(@PathParam("categoryName") String categoryName, PropertiesFilter propertiesFilter) {

        Category category = categoryDao.getByName(categoryName);

        if (category == null) {
            return Results.json().render("error", "category with specified name was not found");
        }

        final Map<Property, List<PropertyValue>> propertyValuesFilterMap = new HashMap<>();

        if (propertiesFilter.propertyValues != null) {
            for (String propertyValueName : propertiesFilter.propertyValues) {
                final PropertyValue propertyValue = propertyDao.getPropertyValueByName(propertyValueName);
                if (propertyValue == null) {
                    return Results.json().render("error", "property value with specified name was not found");
                }

                List<PropertyValue> propertyValues = propertyValuesFilterMap.get(propertyValue.getProperty());
                if (propertyValues == null) {
                    propertyValues = new ArrayList<>();
                    propertyValuesFilterMap.put(propertyValue.getProperty(), propertyValues);
                }

                propertyValues.add(propertyValue);
            }
        }

        SortedSet<PropertyDto> sortedResult = new TreeSet<>();

        // get property values count for specified filter
        final List<PropertyResultItem> propertyValues = getPropertyValuesCount(category, null, propertyValuesFilterMap, false);

        sortedResult.addAll(propertyValues);

        // get additional property values count excluding property items from filter
        for (Map.Entry<Property,List<PropertyValue>> propertyValueEntry:propertyValuesFilterMap.entrySet()) {

            if (propertyValueEntry.getValue().size() > 0) {
                final PropertyResultItem addPropertyValuesItem = getPropertyValuesCount(category, propertyValueEntry.getKey(), propertyValuesFilterMap, true).get(0);
                sortedResult.add(addPropertyValuesItem);
            }
        }

        return Results.json().render("data", sortedResult);
    }

    private List<PropertyResultItem> getPropertyValuesCount(Category category, Property property, Map<Property, List<PropertyValue>> propertiesFilter, Boolean isAdditional) {
        final List<PropertyResultItem> result = new ArrayList<>();

        for (Object item : productDao.countPropertyValuesByCategory(category, property, propertiesFilter)) {
            Object[] itemArr = (Object[]) item;
            final PropertyValue propertyValue = (PropertyValue) itemArr[0];

            final PropertyValueResultItem propertyValueWithCount = new PropertyValueResultItem(propertyValue, (Long) itemArr[1]);

            PropertyResultItem propertyResultItem;

            if (result.size() < 1 || !result.get(result.size() - 1).getName().equals(propertyValue.getProperty().getName())) {
                propertyResultItem = new PropertyResultItem(propertyValue.getProperty(), isAdditional);
                result.add(propertyResultItem);
            } else {
                propertyResultItem = result.get(result.size() - 1);
            }

            propertyResultItem.getPropertyValues().add(propertyValueWithCount);
        }

        return result;
    }
}
