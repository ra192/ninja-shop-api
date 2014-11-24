package controllers;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.persist.Transactional;
import dao.CategoryDao;
import dto.CategoryWithChildrenDto;
import filters.CorsFilter;
import model.Category;
import ninja.FilterWith;
import ninja.Result;
import ninja.Results;
import ninja.jaxy.Path;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yakov_000 on 26.06.2014.
 */

@Singleton
@FilterWith(CorsFilter.class)
@Path("/")
public class CategoryController {

    @Inject
    CategoryDao categoryDao;

    @Transactional
    @Path("categories.json")
    public Result categories() {

        final List<CategoryWithChildrenDto> result = new ArrayList<>();

        for(Category category:categoryDao.listRoots()) {
            result.add(new CategoryWithChildrenDto(category));
        }

        return Results.json().render("data", result);
    }
}
