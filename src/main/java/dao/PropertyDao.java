package dao;

import com.google.inject.Singleton;
import com.google.inject.persist.Transactional;
import model.Property;
import model.PropertyValue;
import ninja.jpa.UnitOfWork;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

/**
 * Created by yakov_000 on 24.06.2014.
 */

@Singleton
public class PropertyDao extends BaseDao<Property> {

    public PropertyDao() {
        super(Property.class);
    }

    public Property getByName(String name) {
        EntityManager entityManager = entityManagerProvider.get();
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();

        CriteriaQuery<Property> criteriaQuery = criteriaBuilder.createQuery(entityClass);
        Root<Property> propertyRoot = criteriaQuery.from(entityClass);
        criteriaQuery.where(criteriaBuilder.equal(propertyRoot.get("name"), name));

        try {
            return entityManager.createQuery(criteriaQuery).getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public PropertyValue getPropertyValueByName(String name) {
        EntityManager entityManager = entityManagerProvider.get();
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();

        CriteriaQuery<PropertyValue> criteriaQuery = criteriaBuilder.createQuery(PropertyValue.class);
        Root<PropertyValue> propertyRoot = criteriaQuery.from(PropertyValue.class);
        criteriaQuery.where(criteriaBuilder.equal(propertyRoot.get("name"), name));

        try {
            return entityManager.createQuery(criteriaQuery).getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public void savePropertyValue(PropertyValue propertyValue) {
        entityManagerProvider.get().persist(propertyValue);
    }
}
