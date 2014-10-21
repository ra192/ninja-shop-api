package controllers;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import dao.CategoryDao;
import dao.ProductDao;
import filters.CorsFilter;
import model.Category;
import model.Product;
import ninja.FilterWith;
import ninja.Result;
import ninja.Results;
import ninja.params.PathParam;

/**
 * Created by yakov_000 on 26.06.2014.
 */
@Singleton
@FilterWith(CorsFilter.class)
public class ProductController {

    @Inject
    ProductDao productDao;

    @Inject
    CategoryDao categoryDao;

    public Result products(@PathParam("categoryName")String categoryName) {

        Category category = categoryDao.getByName(categoryName);

        if(category==null) {
            return Results.json().render("error","category with specified name was not found");
        }

        return Results.json().render("data",productDao.listByCategory(category));
    }

    public Result product(@PathParam("id")Long id) {

        Product product = productDao.get(id);

        if(product!=null)
            return Results.json().render(product);
        else
            return Results.json().render("error","product with specified id was not found");
    }
}
