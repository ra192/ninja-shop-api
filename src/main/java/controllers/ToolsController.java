package controllers;

import annotations.AllowedRoles;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.persist.Transactional;
import dao.CategoryDao;
import dao.ProductDao;
import dao.PropertyDao;
import dto.*;
import filters.CorsFilter;
import filters.SecurityFilter;
import model.Category;
import model.Product;
import model.Property;
import model.PropertyValue;
import ninja.Context;
import ninja.FilterWith;
import ninja.Result;
import ninja.Results;
import org.apache.commons.fileupload.FileItemStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;

/**
 * Created by yakov_000 on 18.06.2014.
 */

@Singleton
@FilterWith({CorsFilter.class, SecurityFilter.class})
public class ToolsController {

    Logger logger = LoggerFactory.getLogger(ToolsController.class);

    @Inject
    PropertyDao propertyDao;

    @Inject
    CategoryDao categoryDao;

    @Inject
    ProductDao productDao;

    @AllowedRoles(roles = {"ROLE_ADMIN"})
    @Transactional
    public Result importData(Context context) {

        try {
            if (!context.isMultipart() && !context.getFileItemIterator().hasNext()) {
                logger.error("uploaded error: no data was provided for import");
                return Results.json().render("error", "no data was provided for import");
            }

            FileItemStream fileItem = context.getFileItemIterator().next();

            XmlMapper xmlMapper = new XmlMapper();

            DataDto dataDto = xmlMapper.readValue(fileItem.openStream(), DataDto.class);

            for (PropertyDto propertyDto : dataDto.getProperties()) {
                String propertyName = propertyDto.getName().trim();
                if (propertyDao.getByName(propertyName) == null) {
                    Property property = new Property();

                    property.setName(propertyName);
                    property.setDisplayName(propertyDto.getDisplayName().trim());

                    propertyDao.save(property);

                    logger.info("Property with name {} was added", property.getName());

                    for (PropertyValueDto propertyValueDto : propertyDto.getPropertyValues()) {
                        PropertyValue propertyValue = new PropertyValue();

                        propertyValue.setName(propertyValueDto.getName().trim());
                        propertyValue.setDisplayName(propertyValueDto.getDisplayName().trim());
                        propertyValue.setProperty(property);

                        propertyDao.savePropertyValue(propertyValue);

                        logger.info("Property value with name {} was added", propertyName);
                    }
                } else {
                    logger.info("Property with name {} already exists", propertyName);
                }
            }

            for (CategoryWithParentAndPropertiesDto categoryDto : dataDto.getCategories()) {
                String categoryName = categoryDto.getName().trim();
                if (categoryDao.getByName(categoryName) == null) {
                    Category category = new Category();

                    category.setName(categoryName);
                    category.setDisplayName(categoryDto.getDisplayName().trim());

                    String parentName = categoryDto.getParent().trim();
                    if (!parentName.isEmpty()) {
                        category.setParent(categoryDao.getByName(parentName));
                    }


                    if (categoryDto.getProperties() != null) {
                        HashSet<Property> properties = new HashSet<>();
                        for (String propertyName : categoryDto.getProperties()) {
                            properties.add(propertyDao.getByName(propertyName.trim()));
                        }
                        category.setProperties(properties);
                    }

                    categoryDao.save(category);

                    logger.info("Category with name {} was added", categoryName);
                } else {
                    logger.info("Category with name {} already exists", categoryName);
                }
            }

            for (ProductDto productDto : dataDto.getProducts()) {
                String productCode = productDto.getCode().trim();
                if (productDao.getByCode(productCode) == null) {
                    Product product = new Product();

                    product.setCode(productCode);
                    product.setDescription(productDto.getDescription().trim());
                    product.setDisplayName(productDto.getDisplayName().trim());
                    product.setImageUrl(productDto.getImageUrl().trim());
                    product.setPrice(productDto.getPrice());
                    product.setCategory(categoryDao.getByName(productDto.getCategory().trim()));

                    HashSet<PropertyValue> propertyValues = new HashSet<PropertyValue>();
                    for (String propertyValueName : productDto.getPropertyValues()) {
                        propertyValues.add(propertyDao.getPropertyValueByName(propertyValueName.trim()));
                    }

                    product.setPropertyValues(propertyValues);

                    productDao.save(product);

                    logger.info("Product with code {} was added", productCode);

                } else {
                    logger.info("Product with code {} already exists", productCode);
                }
            }

            return Results.json().render("result", "uploaded: ".concat(fileItem.getFieldName()));
        } catch (Exception e) {
            logger.error("uploaded error", e);
            return Results.json().render("error", "uploaded error");
        }
    }
}
