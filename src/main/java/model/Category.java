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

    @Column(unique = true,nullable = false)
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

}
