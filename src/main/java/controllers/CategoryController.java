package controllers;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import dao.CategoryDao;
import filters.CorsFilter;
import model.Category;
import model.Property;
import ninja.FilterWith;
import ninja.Result;
import ninja.Results;
import ninja.jpa.UnitOfWork;

import java.util.List;

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

        final List<Category> categories = categoryDao.listRoots();
        for(Category category:categories) {
            expand(category);
        }

        return Results.json().render("data", categories);
    }

    private void expand(Category category) {

        for(Property itm:category.getProperties()){

        }
        for(Category child:category.getChildren()) {
            expand(child);
        }
    }
}
