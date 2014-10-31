package controllers;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import dao.CategoryDao;
import dao.ProductDao;
import dto.ProductDto;
import dto.PropertyDto;
import dto.PropertyValueDto;
import filters.CorsFilter;
import model.Category;
import model.Product;
import model.PropertyValue;
import ninja.FilterWith;
import ninja.Result;
import ninja.Results;
import ninja.jpa.UnitOfWork;
import ninja.params.PathParam;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by yakov_000 on 26.06.2014.
 */

class PropertyValueWithCount extends PropertyValueDto {
    public Long count;
}

@Singleton
@FilterWith(CorsFilter.class)
public class ProductController {

    @Inject
    ProductDao productDao;

    @Inject
    CategoryDao categoryDao;

    @UnitOfWork
    public Result products(@PathParam("categoryName")String categoryName) {

        Category category = categoryDao.getByName(categoryName);

        if(category==null) {
            return Results.json().render("error","category with specified name was not found");
        }

        final List<ProductDto> result = new ArrayList<>();

        for(Product product:productDao.listByCategory(category)) {
            result.add(new ProductDto(product));
        }

        return Results.json().render("data",result);
    }

    @UnitOfWork
    public Result product(@PathParam("id")Long id) {

        Product product = productDao.get(id);

        if(product!=null)
            return Results.json().render(new ProductDto(product));
        else
            return Results.json().render("error","product with specified id was not found");
    }

    public Result properties(@PathParam("categoryName") String categoryName) {

        Category category = categoryDao.getByName(categoryName);

        if(category==null) {
            return Results.json().render("error","category with specified name was not found");
        }

        final List<PropertyDto> result = new ArrayList<>();

        for(Object item:productDao.listPropertyValuesByCategory(category)) {
            Object[] itemArr= (Object[]) item;
            final PropertyValue propertyValue = (PropertyValue) itemArr[0];

            final PropertyValueDto propertyValueDto = new PropertyValueDto();
            propertyValueDto.setName(propertyValue.getName());
            propertyValueDto.setDisplayName(propertyValue.getDisplayName());

            PropertyDto propertyDto;

            if(result.size()<1 || !result.get(result.size()-1).getName().equals(propertyValue.getProperty().getName())) {
               propertyDto=new PropertyDto();
               propertyDto.setName(propertyValue.getProperty().getName());
               propertyDto.setDisplayName(propertyValue.getProperty().getDisplayName());
               propertyDto.setPropertyValues(new ArrayList<PropertyValueDto>());

               result.add(propertyDto);
            } else {
                propertyDto=result.get(result.size()-1);
            }

            propertyDto.getPropertyValues().add(propertyValueDto);
        }

        return Results.json().render("data",result);
    }
}
