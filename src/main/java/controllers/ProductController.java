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

import java.util.ArrayList;
import java.util.Map;

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

        final ArrayList<Map<String,Object>> result = new ArrayList<>();

        for(Product product:productDao.listByCategory(category)) {
            result.add(product.toMap());
        }

        return Results.json().render("data",result);
    }

    public Result product(@PathParam("id")Long id) {

        Product product = productDao.get(id);

        if(product!=null)
            return Results.json().render(product.toMap());
        else
            return Results.json().render("error","product with specified id was not found");
    }
}
