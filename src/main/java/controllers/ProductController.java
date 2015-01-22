package controllers;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.persist.Transactional;
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
import ninja.jaxy.POST;
import ninja.jaxy.Path;
import ninja.params.Param;
import ninja.params.PathParam;
import ninja.validation.JSR303Validation;
import ninja.validation.Validation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

class PropertyValueDoesntExist extends Throwable {
}

@Singleton
@FilterWith(CorsFilter.class)
@Path("/")
public class ProductController {

    Logger logger= LoggerFactory.getLogger(ProductController.class);

    @Inject
    ProductDao productDao;
    @Inject
    CategoryDao categoryDao;
    @Inject
    PropertyDao propertyDao;

    @Transactional
    @Path("products/{categoryName}.json")
    @POST
    public Result products(@PathParam("categoryName") String categoryName, ProductFilter productFilter) {

        Category category = categoryDao.getByName(categoryName);

        if (category == null) {
            return Results.json().render("error", "category with specified name was not found");
        }

        final Map<Property, Set<PropertyValue>> propertyValuesFilterMap;
        try {
            propertyValuesFilterMap = getPropertiesFilter(productFilter.propertyValues);
        } catch (PropertyValueDoesntExist propertyValueDoesntExist) {
            return Results.json().render("error", "property value with specified name was not found");
        }

        final List<ProductDto> result = new ArrayList<>();

        for (Product product : productDao.listByCategory(category, propertyValuesFilterMap, productFilter.orderProperty,
                productFilter.isAsc, productFilter.first, productFilter.max)) {
            result.add(new ProductDto(product));
        }

        final Long count = productDao.countByCategory(category, propertyValuesFilterMap);

        return Results.json().render(new ProductResult(result, count));
    }

    @Transactional
    @Path("product/{code}.json")
    public Result product(@PathParam("code") String code) {

        Product product = productDao.getByCode(code);

        if (product != null)
            return Results.json().render(new ProductDto(product));
        else
            return Results.json().render("error", "product with specified id was not found");
    }

    @Transactional
    @Path("productsProperties/{categoryName}.json")
    @POST
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

    @Transactional
    @Path("search")
    public Result search(@Param("q")String query) {

        final List<ProductDto>result=new ArrayList<>();
        for (Product product:productDao.search(query)) {
            result.add(new ProductDto(product));
        }

        return Results.json().render("data",result);
    }

    @Transactional
    @Path("product/create.json")
    @POST
    public Result create(@JSR303Validation ProductDto productDto, Validation validation) {

        logger.info("Create product method was invoked with following params: {}", productDto.toString());

        if(validation.hasBeanViolations()) {
            logger.error("Specified product object has violations");

            return Results.json().render("error","Specified product object has violations");
        }

        if(productDao.getByCode(productDto.getCode())!=null) {
            logger.error("Product with specified code already exists");

            return Results.json().render("error","Product with specified code already exists");
        }

        final Category category = categoryDao.getByName(productDto.getCategory());
        if(category==null) {
            logger.error("Category with specified name doesn't exist");

            return Results.json().render("error","Category with specified name doesn't exist");
        }


        Set<PropertyValue> propertyValues=new HashSet<>();
        for (String propertyValueName:productDto.getPropertyValues()) {
            final PropertyValue propertyValue = propertyDao.getPropertyValueByName(propertyValueName);
            if(propertyValue==null) {
                logger.error("Property value with name {} doesn't exist",propertyValueName);

                return Results.json().render("error",String.format("Property value with name %s doesn't exist",propertyValueName));
            }

            propertyValues.add(propertyValue);
        }

        final Product product = new Product();
        product.setDisplayName(productDto.getDisplayName());
        product.setCategory(category);
        product.setCode(productDto.getCode());
        product.setDescription(productDto.getDescription());
        product.setImageUrl(productDto.getImageUrl());
        product.setPrice(productDto.getPrice());
        product.setPropertyValues(propertyValues);

        productDao.save(product);

        logger.info("Product was created successfully");

        return Results.json().render("result","ok");
    }

    @Transactional
    @Path("product/update.json")
    @POST
    public Result update(@JSR303Validation ProductDto productDto, Validation validation) {

        logger.info("Update product method was invoked with following params: {}", productDto.toString());

        if(validation.hasBeanViolations()) {
            logger.error("Specified product object has violations");

            return Results.json().render("error","Specified product object has violations");
        }


        final Product product = productDao.getByCode(productDto.getCode());
        if(product ==null) {
            logger.error("Product with specified code doesn't exist");

            return Results.json().render("error","Product with specified code doesn't exist");
        }

        product.getPropertyValues().clear();
        for (String propertyValueName:productDto.getPropertyValues()) {
            final PropertyValue propertyValue = propertyDao.getPropertyValueByName(propertyValueName);
            if(propertyValue==null) {
                logger.error("Property value with name {} doesn't exist",propertyValueName);

                return Results.json().render("error",String.format("Property value with name %s doesn't exist",propertyValueName));
            }

            product.getPropertyValues().add(propertyValue);
        }

        product.setDisplayName(productDto.getDisplayName());
        product.setDescription(productDto.getDescription());
        product.setImageUrl(productDto.getImageUrl());
        product.setPrice(productDto.getPrice());

        productDao.save(product);

        logger.info("Product was updated successfully");

        return Results.json().render("result","ok");
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

    public static class ProductFilter extends PropertiesFilter {
        public String orderProperty = "displayName";
        public Boolean isAsc = true;
        public Integer first = 0;
        public Integer max;
    }

    public static class ProductResult {
        public List<ProductDto> data;
        public Long count;

        public ProductResult(List<ProductDto> data, Long count) {
            this.data = data;
            this.count = count;
        }
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
