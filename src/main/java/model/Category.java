package model;

import javax.persistence.*;
import java.util.*;

/**
 * Created by yakov_000 on 17.06.2014.
 */
@Entity
public class Category {
    private Long id;
    private String name;
    private String displayName;
    private Category parent;
    private Set<Category>children;
    private Set<Property>properties;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    @ManyToOne()
    public Category getParent() {
        return parent;
    }

    @OneToMany(mappedBy = "parent")
    @OrderBy("displayName ASC")
    public Set<Category> getChildren() {
        return children;
    }

    public void setParent(Category parent) {
        this.parent = parent;
    }

    public void setChildren(Set<Category> children) {
        this.children = children;
    }

    @ManyToMany
    public Set<Property> getProperties() {
        return properties;
    }

    public void setProperties(Set<Property> properties) {
        this.properties = properties;
    }

    public Map<String,Object> toMap() {

        final Map<String, Object> result = new HashMap<>();

        result.put("name",name);
        result.put("displayName",displayName);

        final List<Map<String,Object>> childrenMaps=new ArrayList<>();

        for(Category child:children) {
            childrenMaps.add(child.toMap());
         }

        result.put("children",childrenMaps);

        return result;
    }
}
