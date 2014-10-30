package dao;

import com.google.inject.Singleton;
import model.Category;
import model.Product;
import model.Property;
import model.PropertyValue;
import ninja.jpa.UnitOfWork;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Root;
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
    public List<Object> listPropertyValuesByCategory(Category category) {
        EntityManager entityManager = entityManagerProvider.get();
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();

        CriteriaQuery<Object> criteriaQuery = criteriaBuilder.createQuery();
        final Root<Product> productRoot = criteriaQuery.from(entityClass);
        final Join<Product, PropertyValue> propertyValues = productRoot.join("propertyValues");
        final Join<PropertyValue, Property> property = propertyValues.join("property");

        criteriaQuery.where(criteriaBuilder.equal(productRoot.get("category"), category));

        criteriaQuery.groupBy(property, propertyValues);

        criteriaQuery.orderBy(criteriaBuilder.asc(property.get("displayName")), criteriaBuilder.asc(propertyValues.get("displayName")));

        criteriaQuery.multiselect(propertyValues, criteriaBuilder.count(productRoot));

        return entityManager.createQuery(criteriaQuery).getResultList();
    }
}
