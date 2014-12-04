package dao;

import com.google.inject.Singleton;
import model.Category;
import model.Product;
import model.Property;
import model.PropertyValue;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.query.dsl.QueryBuilder;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.awt.print.Book;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by yakov_000 on 17.06.2014.
 */
@Singleton
public class ProductDao extends BaseDao<Product> {


    public ProductDao() {
        super(Product.class);
    }

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

    public List<Product> listByCategory(Category category, Map<Property, Set<PropertyValue>> propertyValuesFilter,
                                        String orderPropertyName, Boolean isOrderAsk, Integer first, Integer max) {
        EntityManager entityManager = entityManagerProvider.get();
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();

        CriteriaQuery<Product> criteriaQuery = criteriaBuilder.createQuery(entityClass);
        Root<Product> productRoot = criteriaQuery.from(entityClass);

        addCategoryAndProperyValuesFilter(category, propertyValuesFilter, criteriaBuilder, criteriaQuery, productRoot);

        //add order
        Order order;
        if (isOrderAsk) {
            order = criteriaBuilder.asc(productRoot.get(orderPropertyName));
        } else {
            order = criteriaBuilder.desc(productRoot.get(orderPropertyName));
        }
        criteriaQuery.orderBy(order);

        final TypedQuery<Product> query = entityManager.createQuery(criteriaQuery);

        //set limits if specified
        query.setFirstResult(first);
        if (max != null)
            query.setMaxResults(max);

        return query.getResultList();
    }

    public Long countByCategory(Category category, Map<Property, Set<PropertyValue>> propertyValuesFilter) {
        EntityManager entityManager = entityManagerProvider.get();
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();

        CriteriaQuery<Object> criteriaQuery = criteriaBuilder.createQuery(Object.class);
        Root<Product> productRoot = criteriaQuery.from(entityClass);

        addCategoryAndProperyValuesFilter(category, propertyValuesFilter, criteriaBuilder, criteriaQuery, productRoot);

        criteriaQuery.select(criteriaBuilder.count(productRoot));

        return (Long) entityManager.createQuery(criteriaQuery).getSingleResult();
    }

    public List<Object> countPropertyValuesByCategory(Category category, Property property,
                                                      Map<Property, Set<PropertyValue>> propertyValuesFilter) {

        EntityManager entityManager = entityManagerProvider.get();
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();

        CriteriaQuery<Object> criteriaQuery = criteriaBuilder.createQuery();
        final Root<Product> productRoot = criteriaQuery.from(entityClass);
        final Join<Product, PropertyValue> propertyValues = productRoot.join("propertyValues");
        final Join<PropertyValue, Property> propertyRoot = propertyValues.join("property");

        final List<Predicate> andPredicates = new ArrayList<>();

        //category filter
        andPredicates.add(criteriaBuilder.equal(productRoot.get("category"), category));

        //property filter if specified
        if (property != null)
            andPredicates.add(criteriaBuilder.equal(propertyValues.get("property"), property));

        final List<PropertyValue> propertyValuesFlat = new ArrayList<>();

        //property values filter excluding speicfied property
        if (propertyValuesFilter != null) {
            for (Map.Entry<Property, Set<PropertyValue>> propertyValuesEntry : propertyValuesFilter.entrySet()) {
                if (!propertyValuesEntry.getKey().equals(property) && propertyValuesEntry.getValue().size() > 0) {
                    addPropertyValuesSubquery(criteriaBuilder, criteriaQuery, productRoot, andPredicates,
                            propertyValuesEntry.getValue());
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

    public List<Product> search(String queryString) {

        EntityManager entityManager = entityManagerProvider.get();
        FullTextEntityManager fullTextEntityManager =
                org.hibernate.search.jpa.Search.getFullTextEntityManager(entityManager);
        // create native Lucene query unsing the query DSL
        QueryBuilder qb = fullTextEntityManager.getSearchFactory()
                .buildQueryBuilder().forEntity(Product.class).get();
        org.apache.lucene.search.Query query = qb
                .keyword()
                .onFields("displayName", "propertyValues.displayName")
                .matching(queryString)
                .createQuery();

        // wrap Lucene query in a javax.persistence.Query
        javax.persistence.Query persistenceQuery =
                fullTextEntityManager.createFullTextQuery(query, Product.class);

        // execute search
        return persistenceQuery.getResultList();
    }

    private void addPropertyValuesSubquery(CriteriaBuilder criteriaBuilder, CriteriaQuery<?> criteriaQuery,
                                           Root<Product> productRoot, List<Predicate> andPredicates,
                                           Set<PropertyValue> propertyValues) {

        final Subquery<Product> subquery = criteriaQuery.subquery(Product.class);
        final Root<Product> productSubRoot = subquery.from(Product.class);
        final Join<Product, PropertyValue> subPropertyValues = productSubRoot.join("propertyValues");

        subquery.where(criteriaBuilder.and(criteriaBuilder.equal(productRoot, productSubRoot),
                subPropertyValues.in(propertyValues)));

        subquery.select(productSubRoot);

        andPredicates.add(criteriaBuilder.exists(subquery));
    }

    private void addCategoryAndProperyValuesFilter(Category category, Map<Property, Set<PropertyValue>> propertyValuesFilter, CriteriaBuilder criteriaBuilder, CriteriaQuery<?> criteriaQuery, Root<Product> productRoot) {
        final List<Predicate> andPredicates = new ArrayList<>();

        //category filter
        andPredicates.add(criteriaBuilder.equal(productRoot.get("category"), category));

        //property values filter if specified
        if (propertyValuesFilter != null && !propertyValuesFilter.isEmpty()) {
            for (Map.Entry<Property, Set<PropertyValue>> propertyValuesEntry : propertyValuesFilter.entrySet()) {
                if (propertyValuesEntry.getValue().size() > 0) {
                    addPropertyValuesSubquery(criteriaBuilder, criteriaQuery, productRoot, andPredicates,
                            propertyValuesEntry.getValue());
                }
            }
        }

        criteriaQuery.where(andPredicates.toArray(new Predicate[andPredicates.size()]));
    }

}
