package controllers;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.persist.Transactional;
import dao.CategoryDao;
import dao.PropertyDao;
import dto.CategoryWithChildrenDto;
import dto.CategoryWithParentAndPropertiesDto;
import filters.CorsFilter;
import model.Category;
import model.Property;
import ninja.FilterWith;
import ninja.Result;
import ninja.Results;
import ninja.jaxy.POST;
import ninja.jaxy.PUT;
import ninja.jaxy.Path;
import ninja.params.PathParam;
import ninja.validation.JSR303Validation;
import ninja.validation.Validation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by yakov_000 on 26.06.2014.
 */

@Singleton
@FilterWith(CorsFilter.class)
@Path("/categories")
public class CategoryController {

    Logger logger= LoggerFactory.getLogger(CategoryController.class);

    @Inject
    CategoryDao categoryDao;

    @Inject
    PropertyDao propertyDao;

    @Transactional
    @Path("")
    public Result categories() {

        final List<CategoryWithChildrenDto> result = new ArrayList<>();

        for(Category category:categoryDao.listRoots()) {
            result.add(new CategoryWithChildrenDto(category));
        }

        return Results.json().render("data", result);
    }

    @Transactional
    @Path("/{name}")
    public Result getByName(@PathParam("name") String name) {

        final Category category = categoryDao.getByName(name);

        final CategoryWithParentAndPropertiesDto result = new CategoryWithParentAndPropertiesDto(category);

        return Results.json().render(result);
    }

    @Transactional
    @Path("")
    @POST
    public Result create(@JSR303Validation CategoryWithParentAndPropertiesDto categoryDto, Validation validation) {

        logger.info("Create category method was invoked with following params: {}",categoryDto.toString());

        if(validation.hasBeanViolations()) {
            logger.error("Specified category object has violations");

            return Results.json().render("error","Specified category object has violations");
        }

        if(categoryDao.getByName(categoryDto.getName())!=null) {
            logger.error("Category with specified name already exists");

            return Results.json().render("error","Category with specified name already exists");
        }

        final Category parent;
        if(categoryDto.getParent()!=null) {
            parent=categoryDao.getByName(categoryDto.getParent());
            if(parent==null) {
                logger.error("Parent category with specified name doesn't exists");

                return Results.json().render("error","Parent category with specified name doesn't exists");
            }
        } else {
            parent=null;
        }

        Set<Property> properties=new HashSet<>();
        for(String propertyName:categoryDto.getProperties()) {
            final Property property = propertyDao.getByName(propertyName);
            if(property==null) {
                logger.error("Property with name {} doesn't exists",propertyName);

                return Results.json().render("error",String.format("Property with name %s doesn't exists",propertyName));
            }
            properties.add(property);
        }

        Category category = new Category();
        category.setDisplayName(categoryDto.getDisplayName());
        category.setName(categoryDto.getName());
        category.setParent(parent);
        category.setProperties(properties);

        categoryDao.save(category);

        logger.info("Category was successfully created");

        return Results.json().render("result","ok");
    }

    @Transactional
    @Path("")
    @PUT
    public Result update(@JSR303Validation CategoryWithParentAndPropertiesDto categoryDto, Validation validation) {

        logger.info("Update category method was invoked with following params: {}",categoryDto.toString());

        if(validation.hasBeanViolations()) {
            logger.error("Specified category object has violations");

            return Results.json().render("error","Specified category object has violations");
        }

        final Category category=categoryDao.getByName(categoryDto.getName());

        if(category==null) {
            logger.error("Category with specified name doesn't exist");

            return Results.json().render("error","Category with specified name doesn't exist");
        }

        category.setDisplayName(categoryDto.getDisplayName());

        categoryDao.save(category);

        logger.info("Category was successfully updated");

        return Results.json().render("result","ok");
    }
}
