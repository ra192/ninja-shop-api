package controllers;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.persist.Transactional;
import dao.PropertyDao;
import dto.PropertyDto;
import dto.PropertyValueDto;
import filters.CorsFilter;
import model.Property;
import model.PropertyValue;
import ninja.FilterWith;
import ninja.Result;
import ninja.Results;
import ninja.jaxy.POST;
import ninja.jaxy.Path;
import ninja.params.PathParam;
import ninja.validation.JSR303Validation;
import ninja.validation.Validation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Роман on 08.01.2015.
 */
@Singleton
@FilterWith(CorsFilter.class)
@Path("/properties")
public class PropertyController {

    Logger logger = LoggerFactory.getLogger(PropertyController.class);

    @Inject
    PropertyDao propertyDao;

    @Transactional
    @Path(".json")
    public Result properties() {

        List<PropertyDto> result = new ArrayList<>();
        for (Property property : propertyDao.list())
            result.add(new PropertyDto(property));

        return Results.json().render("data", result);
    }

    @Transactional
    @Path("/get/{name}.json")
    public Result get(@PathParam("name") String name) {

        final Property property = propertyDao.getByName(name);

        if (property == null) {
            logger.info("Property with specified name doesn't exist");

            return Results.json().render("error", "Property with specified name doesn't exist");
        }

        final PropertyDto result = new PropertyDto(property);

        for (PropertyValue propertyValue : property.getPropertyValues())
            result.getPropertyValues().add(new PropertyValueDto(propertyValue));

        return Results.json().render(result);
    }

    @Transactional
    @Path("/create.json")
    @POST
    public Result create(@JSR303Validation PropertyDto propertyDto, Validation validation) {

        logger.info("Create property method was invoked with following params {}", propertyDto.toString());

        if (validation.hasBeanViolations()) {
            logger.error("Specified property object has violations");

            return Results.json().render("error", "Specified property object has violations");
        }

        if (propertyDao.getByName(propertyDto.getName()) != null) {
            logger.error("Property with specified name already exists");

            return Results.json().render("error", "Property with specified name already exists");
        }

        final Property property = new Property();
        property.setName(propertyDto.getName());
        property.setDisplayName(propertyDto.getDisplayName());

        propertyDao.save(property);

        logger.info("Property was created successfully");

        for (PropertyValueDto propertyValueDto : propertyDto.getPropertyValues()) {
            PropertyValue propertyValue = propertyDao.getPropertyValueByName(propertyValueDto.getName());
            if (propertyValue != null)
                logger.info("Property value with {} name already exists", propertyValueDto.getName());
            else {
                propertyValue = new PropertyValue();
                propertyValue.setDisplayName(propertyValueDto.getDisplayName());
                propertyValue.setName(propertyValueDto.getName());
                propertyValue.setProperty(property);

                propertyDao.savePropertyValue(propertyValue);

                logger.info("Property value with {} name was created successfully", propertyValueDto.getName());
            }
        }

        return Results.json().render("result", "ok");
    }

    @Transactional
    @Path("/update.json")
    @POST
    public Result update(@JSR303Validation PropertyDto propertyDto, Validation validation) {

        logger.info("Update property method was invoked with following params {}", propertyDto.toString());

        if (validation.hasBeanViolations()) {
            logger.error("Specified property object has violations");

            return Results.json().render("error", "Specified property object has violations");
        }

        final Property property = propertyDao.getByName(propertyDto.getName());

        if (property == null) {
            logger.error("Property with specified name doesn't exist");

            return Results.json().render("error", "Property with specified name doesn't exist");
        }

        property.setDisplayName(propertyDto.getDisplayName());

        propertyDao.save(property);

        logger.info("Property was updated successfully");

        return Results.json().render("result", "ok");
    }
}
