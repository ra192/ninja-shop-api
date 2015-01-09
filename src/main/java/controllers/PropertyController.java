package controllers;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import dao.PropertyDao;
import dto.PropertyDto;
import filters.CorsFilter;
import model.Property;
import ninja.FilterWith;
import ninja.Result;
import ninja.Results;
import ninja.jaxy.Path;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Роман on 08.01.2015.
 */
@Singleton
@FilterWith(CorsFilter.class)
@Path("/properties")
public class PropertyController {

    @Inject
    PropertyDao propertyDao;

    @Path(".json")
    public Result properties() {

        List<PropertyDto> result = new ArrayList<>();
        for (Property property : propertyDao.list())
            result.add(new PropertyDto(property));

        return Results.json().render("data", result);
    }
}
