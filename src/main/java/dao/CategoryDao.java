package dao;

import model.Category;
import model.Property;
import ninja.jpa.UnitOfWork;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;

/**
 * Created by yakov_000 on 25.06.2014.
 */
public class CategoryDao extends BaseDao<Category> {

    public CategoryDao() {
        super(Category.class);
    }

    @UnitOfWork
    public Category getByName(String name) {
        EntityManager entityManager = entityManagerProvider.get();
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();

        CriteriaQuery<Category> criteriaQuery = criteriaBuilder.createQuery(entityClass);
        Root<Category> categoryRoot = criteriaQuery.from(entityClass);
        criteriaQuery.where(criteriaBuilder.equal(categoryRoot.get("name"), name));

        try {
            return entityManager.createQuery(criteriaQuery).getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    @UnitOfWork
    public List<Category>listRoots() {
        EntityManager entityManager = entityManagerProvider.get();
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();

        CriteriaQuery<Category> criteriaQuery = criteriaBuilder.createQuery(entityClass);

        Root<Category> categoryRoot = criteriaQuery.from(entityClass);
        criteriaQuery.where(criteriaBuilder.isNull(categoryRoot.get("parent")));

        return entityManager.createQuery(criteriaQuery).getResultList();
    }

    @UnitOfWork
    public List<Category>listByParent(Category parent) {
        EntityManager entityManager = entityManagerProvider.get();
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();

        CriteriaQuery<Category> criteriaQuery = criteriaBuilder.createQuery(entityClass);

        Root<Category> categoryRoot = criteriaQuery.from(entityClass);
        criteriaQuery.where(criteriaBuilder.equal(categoryRoot.get("parent"), parent));

        return entityManager.createQuery(criteriaQuery).getResultList();
    }
}
