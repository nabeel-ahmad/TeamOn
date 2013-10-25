/**
 * Created by IntelliJ IDEA.
 * User: imran.aftab
 * Date: May 26, 2010
 * Time: 3:22:51 PM
 */

package com.spikenow.dao;

import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.springframework.dao.DataAccessException;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

/**
 * A Generic Data Access Object class to provide generic methods for data access using Hibernate.
 * It uses a support class from Spring Framework which facilitates data access by handling the Session itself
 * <p/>
 * Get the HibernateTemplate for this DAO, pre-initialized with the SessionFactory or set explicitly
 * using getHibernateTemplate()
 */
public class HibernateGenericDAO<T> extends HibernateDaoSupport {
    private Class<T> persistentClass;

    private static final Logger LOGGER = Logger.getLogger(HibernateGenericDAO.class);


    public HibernateGenericDAO(Class<T> persistentClass) {
        super();
        this.persistentClass = persistentClass;
    }

    public Class<T> getPersistentClass() {
        return persistentClass;
    }


    public Criteria createCriteria() {
        try {
            return getSession().createCriteria(getPersistentClass());
        } catch (HibernateException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return null;
    }

    public Query getNamedQuery(String queryName) {
        LOGGER.info("Getting Named Query");
        return getSession().getNamedQuery(queryName);
    }


    //Returns the persistent instance of the given entity class with the given identifier, or null if not found.

    public T findById(Integer id) {
        LOGGER.info("\nDAO - findById. id: " + id + " from " + getPersistentClass());
        T entity = null;
        try {
            entity = (T) getHibernateTemplate().get(getPersistentClass(), id);
            if (entity != null) {
                logger.info("Entity Found");
            } else {
                logger.info("No entity Found");
            }
        } catch (DataAccessException dae) {
            LOGGER.error(dae.getMessage(), dae);
            throw dae;
        } catch (RuntimeException re) {
            LOGGER.error(re.getMessage(), re);
            throw re;
        }
        return entity;
    }

    //Returns List of all persistent instances of the given entity class, by using loadAll().

    public List<T> findAll() {
        LOGGER.info("\nDAO - findAll. from " + getPersistentClass());
        List<T> results = null;
        try {
            results = getHibernateTemplate().loadAll(getPersistentClass());
            if (results != null) {
                LOGGER.info("findAll successful, result size: " + results.size());
            }
        } catch (DataAccessException dae) {
            LOGGER.error(dae.getMessage(), dae);
            throw dae;
        } catch (RuntimeException re) {
            LOGGER.error(re.getMessage(), re);
            throw re;
        }
        return results;
    }

    //Persists the given transient instance and returns saved object

    public T insert(T transientInstance) throws Exception {
        LOGGER.info("\nDAO - insert. for " + getPersistentClass());
        try {

            getHibernateTemplate().persist(transientInstance);
            LOGGER.info("Persist successful");
            return transientInstance;
        } catch (RuntimeException re) {
            LOGGER.error("Persist failed" + re.getMessage(), re);
            throw re;
        }
    }

    //Merges the given detached instance and returns saved object

    public T update(T detachedInstance) {
        LOGGER.info("update " + getPersistentClass());
        try {

            T result = (T) getHibernateTemplate().merge(detachedInstance);
            LOGGER.info("Merge successful");
            return result;
        } catch (RuntimeException re) {
            LOGGER.error("Merge failed" + re.getMessage(), re);
            throw re;
        } catch (Exception e) {
            LOGGER.error("Merge failed" + e.getMessage(), e);
        }
        return null;
    }

    //Updates the row_status to -1

    public boolean delete(T persistentInstance) {
        LOGGER.info("delete " + getPersistentClass());
        boolean isDeleted = true;
        try {
            getHibernateTemplate().merge(persistentInstance);
            LOGGER.info("Delete successful");
        } catch (RuntimeException re) {
            isDeleted = false;
            LOGGER.error("Delete failed" + re.getMessage(), re);
            throw re;
        } catch (Exception e) {
            isDeleted = false;
            LOGGER.error("Delete failed" + e.getMessage(), e);
            throw new RuntimeException(e);
        }
        return isDeleted;
    }

    public void refresh(T entity) {
        LOGGER.info("refresh");
        try {
            getHibernateTemplate().refresh(entity);
        } catch (DataAccessException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }


/*
   public T findByIdUsingLoad(Integer id) {
        return this.findByIdUsingLockInLoad(id, false);
    }

    //Returns the persistent instance of the given entity class with the given identifier, throwing an exception if not found.
    @SuppressWarnings("unchecked")
    public T findByIdUsingLockInLoad(Integer id, boolean lock) {
        LOGGER.info("\nDAO - findByIdUsingLoad. id: " + id + " from " + getPersistentClass());
        T entity = null;
        try {
            if (lock) {
                entity = (T) getHibernateTemplate().load(getPersistentClass(), id, LockMode.UPGRADE);
                if (entity != null) {
                    logger.info("Entity Found");
                }
            } else {
                entity = (T) getHibernateTemplate().load(getPersistentClass(), id);
            }
        } catch (DataAccessException dae) {
            LOGGER.error(dae.getMessage(), dae);
            throw dae;
        } catch (RuntimeException re) {
            LOGGER.error(re.getMessage(), re);
            throw re;
        }
        return entity;
    }


    //Executes a query based on the given example entity object.
    public List<T> findByExample(T exampleInstance) {
        LOGGER.info("\nDAO - findByExample. from " + getPersistentClass());
        List<T> results = null;
        try {
            results = (List<T>) createCriteria().add(Example.create(exampleInstance)).list();
            if (PropertyValidator.isValid(results)) {
                LOGGER.info("find by example successful, result size: " + results.size());
            }
        } catch (DataAccessException dae) {
            LOGGER.error(dae.getMessage(), dae);
            throw dae;
        } catch (RuntimeException re) {
            LOGGER.error(re.getMessage(), re);
            throw re;
        }
        return results;
    }

    @SuppressWarnings("unchecked")
    public List<T> findByExample(T exampleInstance, String... excludeProperty) {
        LOGGER.info("\nDAO - findByExample. from " + getPersistentClass());
        List<T> results = null;
        try {
            Criteria criteria = this.createCriteria();
            Example example = Example.create(exampleInstance);
            for (String exclude : excludeProperty) {
                example.excludeProperty(exclude);
            }
            criteria.add(example);
            results = criteria.list();
            if (PropertyValidator.isValid(results)) {
                LOGGER.info("find by example successful, result size: " + results.size());
            }
        } catch (DataAccessException dae) {
            LOGGER.error(dae.getMessage(), dae);
            throw dae;
        } catch (RuntimeException re) {
            LOGGER.error(re.getMessage(), re);
            throw re;
        }
        return results;
    }

    @SuppressWarnings("unchecked")
    protected List<T> findByCriteria(org.hibernate.criterion.Criterion... criterion) {
        org.hibernate.Session session = getSession();
        org.hibernate.Criteria crit = session.createCriteria(getPersistentClass());
        for (org.hibernate.criterion.Criterion c : criterion) {
            crit.add(c);
        }
        return crit.list();
    }

    protected List<?> findByCriteria(Class<?> clazz, org.hibernate.criterion.Criterion... criterion) {
        org.hibernate.Session session = getSession();
        org.hibernate.Criteria crit = session.createCriteria(clazz);
        for (org.hibernate.criterion.Criterion c : criterion) {
            crit.add(c);
        }
        return crit.list();
    }

    @SuppressWarnings("unchecked")
    protected T findUniqueEntityByCriteria(Class<?> clazz, org.hibernate.criterion.Criterion... criterion) {
        org.hibernate.Session session = getSession();
        org.hibernate.Criteria crit
                = session.createCriteria(clazz);
        for (org.hibernate.criterion.Criterion c : criterion) {
            crit.add(c);
        }
        return (T) crit.uniqueResult();
    }
*/


    //Returns HastSet of all persistent instances of the given entity class, by using findByCriteria.
    /*public HashSet<T> findAllByCriteria() {
        LOGGER.info("\nDAO - findAllByCriteria. from " + getPersistentClass());
        HashSet<T> results = new HashSet<T>();
        try {
            results.addAll(findByCriteria());
            LOGGER.info("listing successful");
        } catch (DataAccessException dae) {
            LOGGER.error(dae.getMessage(), dae);
            throw dae;
        } catch (RuntimeException re) {
            LOGGER.error(re.getMessage(), re);
            throw re;
        }
        return results;
    }

    // Executes a query based on a given Hibernate criteria object and returns unique object
    @SuppressWarnings("unchecked")
    public T findUniqueEntityByCriteria(Criterion... criterion) {
        Criteria criteria = this.createCriteria();
        for (org.hibernate.criterion.Criterion c : criterion) {
            criteria.add(c);
        }
        return (T) criteria.uniqueResult();
    }*/

}