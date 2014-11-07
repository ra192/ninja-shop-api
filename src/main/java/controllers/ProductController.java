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

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

@Singleton
@FilterWith(CorsFilter.class)
public class ProductController {

    public static class PropertiesFilter {
        public List<List<String>> propertyValues;
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
            this.count=count;
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

        SortedSet<PropertyDto> sortedResult=new TreeSet<>();

        // get property values count for specified filter
        final List<PropertyResultItem> propertyValues = getPropertyValuesCount(category, null, propertiesFilter.propertyValues, null, false);

        sortedResult.addAll(propertyValues);

        // get additional property values count excluding property items from filter
        if(propertiesFilter.propertyValues!=null) {
            for (int i = 0; i < propertiesFilter.propertyValues.size(); i++) {

                if (propertiesFilter.propertyValues.get(i).size() > 0) {
                    final Property property = propertyDao.getPropertyValueByName(propertiesFilter.propertyValues.get(i).get(0)).getProperty();

                    List<List<String>> subFilter = new ArrayList<>();
                    subFilter.addAll(propertiesFilter.propertyValues.subList(0, i));
                    subFilter.addAll(propertiesFilter.propertyValues.subList(i + 1, propertiesFilter.propertyValues.size()));

                    final PropertyResultItem addPropertyValuesItem = getPropertyValuesCount(category, property, subFilter, propertiesFilter.propertyValues.get(i), true).get(0);
                    sortedResult.add(addPropertyValuesItem);
                }
            }
        }

        return Results.json().render("data",sortedResult);
    }

    private List<PropertyResultItem> getPropertyValuesCount(Category category, Property property, List<List<String>> propertiesFilter, List<String> excludedProperties, Boolean isAdditional) {
        final List<PropertyResultItem> result = new ArrayList<>();

        for (Object item : productDao.countPropertyValuesByCategory(category, property, propertiesFilter, excludedProperties)) {
            Object[] itemArr = (Object[]) item;
            final PropertyValue propertyValue = (PropertyValue) itemArr[0];

            final PropertyValueResultItem propertyValueWithCount = new PropertyValueResultItem(propertyValue, (Long) itemArr[1]);

            PropertyResultItem propertyResultItem;

            if (result.size() < 1 || !result.get(result.size() - 1).getName().equals(propertyValue.getProperty().getName())) {
                propertyResultItem = new PropertyResultItem(propertyValue.getProperty(),isAdditional);
                result.add(propertyResultItem);
            } else {
                propertyResultItem = result.get(result.size() - 1);
            }

            propertyResultItem.getPropertyValues().add(propertyValueWithCount);
        }

        return result;
    }
}
