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

class PropertyValueDoesntExist extends Throwable {
}

@Singleton
@FilterWith(CorsFilter.class)
public class ProductController {

    @Inject
    ProductDao productDao;
    @Inject
    CategoryDao categoryDao;
    @Inject
    PropertyDao propertyDao;

    @UnitOfWork
    public Result products(@PathParam("categoryName") String categoryName, PropertiesFilter propertiesFilter) {

        Category category = categoryDao.getByName(categoryName);

        if (category == null) {
            return Results.json().render("error", "category with specified name was not found");
        }

        final Map<Property, Set<PropertyValue>> propertyValuesFilterMap;
        try {
            propertyValuesFilterMap = getPropertiesFilter(propertiesFilter.propertyValues);
        } catch (PropertyValueDoesntExist propertyValueDoesntExist) {
            return Results.json().render("error", "property value with specified name was not found");
        }

        final List<ProductDto> result = new ArrayList<>();

        for (Product product : productDao.listByCategory(category, propertyValuesFilterMap)) {
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

        final Map<Property, Set<PropertyValue>> propertyValuesFilterMap;
        try {
            propertyValuesFilterMap = getPropertiesFilter(propertiesFilter.propertyValues);
        } catch (PropertyValueDoesntExist propertyValueDoesntExist) {
            return Results.json().render("error", "property value with specified name was not found");
        }


        SortedSet<PropertyResultItem> sortedResult = new TreeSet<>(new Comparator<PropertyResultItem>() {
            @Override
            public int compare(PropertyResultItem o1, PropertyResultItem o2) {
                return o1.getDisplayName().compareTo(o2.getDisplayName());
            }
        });

        // get property values count for specified filter
        final List<PropertyResultItem> propertyValues = getPropertyValuesCount(category, null, propertyValuesFilterMap, false);

        sortedResult.addAll(propertyValues);

        // get additional property values count excluding property items from filter
        for (Map.Entry<Property, Set<PropertyValue>> propertyValueEntry : propertyValuesFilterMap.entrySet()) {

            if (propertyValueEntry.getValue().size() > 0) {
                final List<PropertyResultItem> addPropertyValuesItem = getPropertyValuesCount(category, propertyValueEntry.getKey(), propertyValuesFilterMap, true);
                if (addPropertyValuesItem.size() > 0)
                    sortedResult.add(addPropertyValuesItem.get(0));
            }
        }

        final List<PropertyDto> selectedProperties = getSelectedProperties(propertyValuesFilterMap);

        return Results.json().render(new PropertiesResult(sortedResult, selectedProperties));
    }

    private List<PropertyResultItem> getPropertyValuesCount(Category category, Property property, Map<Property, Set<PropertyValue>> propertiesFilter, Boolean isAdditional) {
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

    private Map<Property, Set<PropertyValue>> getPropertiesFilter(List<String> propertyValueNames) throws PropertyValueDoesntExist {
        final Map<Property, Set<PropertyValue>> result = new TreeMap<>(new Comparator<Property>() {
            @Override
            public int compare(Property o1, Property o2) {
                return o1.getDisplayName().compareTo(o2.getDisplayName());
            }
        });

        if (propertyValueNames != null) {
            for (String propertyValueName : propertyValueNames) {
                final PropertyValue propertyValue = propertyDao.getPropertyValueByName(propertyValueName);
                if (propertyValue == null) {
                    throw new PropertyValueDoesntExist();
                }

                Set<PropertyValue> propertyValues = result.get(propertyValue.getProperty());
                if (propertyValues == null) {
                    propertyValues = new TreeSet<>(new Comparator<PropertyValue>() {
                        @Override
                        public int compare(PropertyValue o1, PropertyValue o2) {
                            return o1.getDisplayName().compareTo(o2.getDisplayName());
                        }
                    });
                    result.put(propertyValue.getProperty(), propertyValues);
                }

                propertyValues.add(propertyValue);
            }
        }

        return result;
    }

    private List<PropertyDto> getSelectedProperties(Map<Property, Set<PropertyValue>> propertiesFilter) {
        final List<PropertyDto> result = new ArrayList<>();

        for (Map.Entry<Property, Set<PropertyValue>> propertiesFilterEntry : propertiesFilter.entrySet()) {
            final PropertyDto propertyDto = new PropertyDto(propertiesFilterEntry.getKey());
            for (PropertyValue propertyValue : propertiesFilterEntry.getValue()) {
                propertyDto.getPropertyValues().add(new PropertyValueDto(propertyValue));
            }
            result.add(propertyDto);
        }

        return result;
    }

    public static class PropertiesFilter {
        public List<String> propertyValues;
    }

    public static class PropertiesResult {
        public Set<PropertyResultItem> data;
        public List<PropertyDto> selectedProperties;

        public PropertiesResult(Set<PropertyResultItem> data, List<PropertyDto> selectedProperties) {
            this.data = data;
            this.selectedProperties = selectedProperties;
        }
    }

    public static class PropertyResultItem extends PropertyDto {

        public Boolean isAdditional;

        public PropertyResultItem(Property property, Boolean isAdditional) {
            super(property);
            this.isAdditional = isAdditional;
        }
    }

    public static class PropertyValueResultItem extends PropertyValueDto {
        public Long count;

        public PropertyValueResultItem(PropertyValue propertyValue, Long count) {
            super(propertyValue);
            this.count = count;
        }
    }
}
