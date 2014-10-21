package dao;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.persist.Transactional;
import ninja.jpa.UnitOfWork;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import java.util.List;

/**
 * Created by yakov_000 on 24.06.2014.
 */
public abstract class BaseDao<T> {
    protected Class<T> entityClass;

    @Inject
    Provider<EntityManager> entityManagerProvider;

    protected BaseDao(Class<T>entityClass) {
        this.entityClass=entityClass;
    }

    @UnitOfWork
    public T get(Long id) {
        return entityManagerProvider.get().find(entityClass,id);
    }

    @UnitOfWork
    public List<T> list() {
        EntityManager entityManager = entityManagerProvider.get();
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();

        CriteriaQuery<T> criteriaQuery = criteriaBuilder.createQuery(entityClass);
        criteriaQuery.from(entityClass);

        return entityManager.createQuery(criteriaQuery).getResultList();
    }

    @Transactional
    public void save(T entity) {
        entityManagerProvider.get().persist(entity);
    }
}
