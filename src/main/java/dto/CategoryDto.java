package dto;

import model.Category;

/**
 * Created by yakov_000 on 31.10.2014.
 */
public abstract class CategoryDto {

    String name;
    String displayName;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    protected CategoryDto() {
    }

    protected CategoryDto(Category category) {
        name=category.getName();
        displayName=category.getDisplayName();
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("");
        sb.append("name='").append(name).append('\'');
        sb.append(", displayName='").append(displayName).append('\'');
        return sb.toString();
    }
}
