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
    public List<Object> listPropertyValuesByCategory(Category category, List<List<String>> propertyValueNamesFilter) {
        EntityManager entityManager = entityManagerProvider.get();
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();

        CriteriaQuery<Object> criteriaQuery = criteriaBuilder.createQuery();
        final Root<Product> productRoot = criteriaQuery.from(entityClass);
        final Join<Product, PropertyValue> propertyValues = productRoot.join("propertyValues");
        final Join<PropertyValue, Property> property = propertyValues.join("property");

        final ArrayList<Predicate> andPredicates = new ArrayList<>();
        andPredicates.add(criteriaBuilder.equal(productRoot.get("category"), category));

        if (propertyValueNamesFilter != null) {
            for (List<String> orItem : propertyValueNamesFilter) {
                final List<Predicate> propertyValuesOrPredicateList = new ArrayList<>();

                final Subquery<Product> subquery = criteriaQuery.subquery(Product.class);
                final Root<Product> productSubRoot = subquery.from(Product.class);
                final Join<Product, PropertyValue> subPropertyValues = productSubRoot.join("propertyValues");

                for (String propertyValueName : orItem) {
                    propertyValuesOrPredicateList.add(criteriaBuilder.equal(subPropertyValues.get("name"), propertyValueName));
                }

                subquery.where(criteriaBuilder.and(criteriaBuilder.equal(productRoot,productSubRoot),
                        criteriaBuilder.or(propertyValuesOrPredicateList.toArray(new Predicate[propertyValuesOrPredicateList.size()]))));

                subquery.select(productSubRoot);

                andPredicates.add(criteriaBuilder.exists(subquery));
            }
        }

        criteriaQuery.where(andPredicates.toArray(new Predicate[andPredicates.size()]));

        criteriaQuery.groupBy(property, propertyValues);

        criteriaQuery.orderBy(criteriaBuilder.asc(property.get("displayName")), criteriaBuilder.asc(propertyValues.get("displayName")));

        criteriaQuery.multiselect(propertyValues, criteriaBuilder.count(productRoot));

        return entityManager.createQuery(criteriaQuery).getResultList();
    }
}
