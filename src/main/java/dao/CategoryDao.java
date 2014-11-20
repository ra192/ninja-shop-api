package dao;

import com.google.inject.Singleton;
import model.Category;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;

/**
 * Created by yakov_000 on 25.06.2014.
 */
@Singleton
public class CategoryDao extends BaseDao<Category> {

    public CategoryDao() {
        super(Category.class);
    }

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

    public List<Category>listRoots() {
        EntityManager entityManager = entityManagerProvider.get();
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();

        CriteriaQuery<Category> criteriaQuery = criteriaBuilder.createQuery(entityClass);

        Root<Category> categoryRoot = criteriaQuery.from(entityClass);
        criteriaQuery.where(criteriaBuilder.isNull(categoryRoot.get("parent")));

        criteriaQuery.orderBy(criteriaBuilder.asc(categoryRoot.get("displayName")));

        return entityManager.createQuery(criteriaQuery).getResultList();
    }

    public List<Category>listByParent(Category parent) {
        EntityManager entityManager = entityManagerProvider.get();
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();

        CriteriaQuery<Category> criteriaQuery = criteriaBuilder.createQuery(entityClass);

        Root<Category> categoryRoot = criteriaQuery.from(entityClass);
        criteriaQuery.where(criteriaBuilder.equal(categoryRoot.get("parent"), parent));

        return entityManager.createQuery(criteriaQuery).getResultList();
    }
}
