package dao;

import com.google.inject.Singleton;
import model.User;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

/**
 * Created by yakov_000 on 17.11.2014.
 */
@Singleton
public class UserDao extends BaseDao<User> {

    public UserDao() {
        super(User.class);
    }

    public User getByUserId(Long userId) {
        final EntityManager entityManager = entityManagerProvider.get();
        final CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();

        final CriteriaQuery<User> criteriaQuery = criteriaBuilder.createQuery(entityClass);
        final Root<User> userRoot = criteriaQuery.from(entityClass);
        criteriaQuery.where(criteriaBuilder.equal(userRoot.get("userId"),userId));

        try {
            return entityManager.createQuery(criteriaQuery).getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }
}
