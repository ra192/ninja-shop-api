package dao;

import com.google.inject.Singleton;
import model.Category;
import model.Product;
import model.Property;
import model.PropertyValue;
import ninja.jpa.UnitOfWork;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by yakov_000 on 17.06.2014.
 */
@Singleton
public class ProductDao extends BaseDao<Product> {


    public ProductDao() {
        super(Product.class);
    }

    @UnitOfWork
    public Product getByCode(String code) {
        EntityManager entityManager = entityManagerProvider.get();
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();

        CriteriaQuery<Product> criteriaQuery = criteriaBuilder.createQuery(entityClass);
        Root<Product> propertyRoot = criteriaQuery.from(entityClass);
        criteriaQuery.where(criteriaBuilder.equal(propertyRoot.get("code"), code));

        try {
            return entityManager.createQuery(criteriaQuery).getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    @UnitOfWork
    public List<Product> listByCategory(Category category) {
        EntityManager entityManager = entityManagerProvider.get();
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();

        CriteriaQuery<Product> criteriaQuery = criteriaBuilder.createQuery(entityClass);
        Root<Product> propertyRoot = criteriaQuery.from(entityClass);
        criteriaQuery.where(criteriaBuilder.equal(propertyRoot.get("category"), category));

        return entityManager.createQuery(criteriaQuery).getResultList();
    }

    @UnitOfWork
    public List<Object> countPropertyValuesByCategory(Category category, Property property,
                                                      Map<Property,List<PropertyValue>> propertyValueNamesFilter) {

        EntityManager entityManager = entityManagerProvider.get();
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();

        CriteriaQuery<Object> criteriaQuery = criteriaBuilder.createQuery();
        final Root<Product> productRoot = criteriaQuery.from(entityClass);
        final Join<Product, PropertyValue> propertyValues = productRoot.join("propertyValues");
        final Join<PropertyValue, Property> propertyRoot = propertyValues.join("property");

        final ArrayList<Predicate> andPredicates = new ArrayList<>();

        //category filter
        andPredicates.add(criteriaBuilder.equal(productRoot.get("category"), category));

        //property filter if specified
        if (property != null)
            andPredicates.add(criteriaBuilder.equal(propertyValues.get("property"), property));

        final List<PropertyValue> propertyValuesFlat = new ArrayList<>();

        //property values filter excluding speicfied property
        if (propertyValueNamesFilter != null) {
            for (Map.Entry<Property, List<PropertyValue>> propertyValuesEntry : propertyValueNamesFilter.entrySet()) {
                if(!propertyValuesEntry.getKey().equals(property) && propertyValuesEntry.getValue().size()>0) {

                    final Subquery<Product> subquery = criteriaQuery.subquery(Product.class);
                    final Root<Product> productSubRoot = subquery.from(Product.class);
                    final Join<Product, PropertyValue> subPropertyValues = productSubRoot.join("propertyValues");

                    propertyValuesFlat.addAll(propertyValuesEntry.getValue());

                    subquery.where(criteriaBuilder.and(criteriaBuilder.equal(productRoot, productSubRoot),
                            subPropertyValues.in(propertyValuesEntry.getValue())));

                    subquery.select(productSubRoot);

                    andPredicates.add(criteriaBuilder.exists(subquery));
                }

                propertyValuesFlat.addAll(propertyValuesEntry.getValue());
            }
        }

        //add excluded property
        if (propertyValuesFlat.size() > 0)
            //exclude specified properties filter
            andPredicates.add(propertyValues.in(propertyValuesFlat).not());

        criteriaQuery.where(andPredicates.toArray(new Predicate[andPredicates.size()]));

        criteriaQuery.groupBy(propertyRoot, propertyValues);

        criteriaQuery.orderBy(criteriaBuilder.asc(propertyRoot.get("displayName")), criteriaBuilder.asc(propertyValues.get("displayName")));

        criteriaQuery.multiselect(propertyValues, criteriaBuilder.count(productRoot));

        return entityManager.createQuery(criteriaQuery).getResultList();
    }
}
