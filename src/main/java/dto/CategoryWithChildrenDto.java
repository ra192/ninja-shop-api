package dto;

import model.Category;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yakov_000 on 31.10.2014.
 */
public class CategoryWithChildrenDto extends CategoryDto {

    List<CategoryWithChildrenDto>children;

    public List<CategoryWithChildrenDto> getChildren() {
        return children;
    }

    public void setChildren(List<CategoryWithChildrenDto> children) {
        this.children = children;
    }

    public CategoryWithChildrenDto(Category category) {
        super(category);
        children=new ArrayList<>();
        for(Category child:category.getChildren()) {
            children.add(new CategoryWithChildrenDto(child));
        }
    }
}
