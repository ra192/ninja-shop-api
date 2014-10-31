package controllers;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import dao.CategoryDao;
import dto.CategoryWithChildrenDto;
import filters.CorsFilter;
import model.Category;
import ninja.FilterWith;
import ninja.Result;
import ninja.Results;
import ninja.jpa.UnitOfWork;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by yakov_000 on 26.06.2014.
 */

@Singleton
@FilterWith(CorsFilter.class)
public class CategoryController {

    @Inject
    CategoryDao categoryDao;

    @UnitOfWork
    public Result categories() {

        final List<CategoryWithChildrenDto> result = new ArrayList<>();

        for(Category category:categoryDao.listRoots()) {
            result.add(new CategoryWithChildrenDto(category));
        }

        return Results.json().render("data", result);
    }
}
