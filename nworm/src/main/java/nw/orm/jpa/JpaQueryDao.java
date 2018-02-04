package nw.orm.jpa;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import nw.orm.core.exception.NwormQueryException;
import nw.orm.core.query.QueryParameter;
import nw.orm.core.query.SQLModifier;
import nw.orm.dao.QueryDao;

public class JpaQueryDao extends JpaDaoBase implements QueryDao {

	public JpaQueryDao(EntityManagerFactory em, boolean managedTransaction) {
		super(em, managedTransaction);
	}

	@Override
	public <T> T query(Class<T> resultClass, String jpql, QueryParameter... parameters) {
		EntityManager mgr = getEntityManager();
		T res = null;
		try {
			TypedQuery<T> query = mgr.createQuery(jpql, resultClass);
			setParameters(query, parameters);
			res = query.getSingleResult();
			commit(mgr);
		} catch (Exception e) {
			rollback(mgr);
			throw new NwormQueryException("Nw.orm Exception", e);
		}
		return res;
	}

	@Override
	public <T> List<T> queryList(Class<T> resultClass, String jpql, QueryParameter... parameters) {
		EntityManager mgr = getEntityManager();
		List<T> res = new ArrayList<T>();
		try {
			TypedQuery<T> query = mgr.createQuery(jpql, resultClass);
			setParameters(query, parameters);
			res = query.getResultList();
			commit(mgr);
		} catch (Exception e) {
			rollback(mgr);
			throw new NwormQueryException("Nw.orm Exception", e);
		}
		return res;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> List<T> getBySQL(Class<T> returnClazz, String sql, SQLModifier sqlMod, QueryParameter... params) {
		EntityManager mgr = getEntityManager();
		List<T> res = new ArrayList<T>();
		try {
			Query query = mgr.createNativeQuery(sql, returnClazz);
			setParameters(query, params);
			
			if(sqlMod.isPaginated()) {
				query.setFirstResult(sqlMod.getPageIndex());
				query.setMaxResults(sqlMod.getMaxResult());
			}
			res = query.getResultList();
			commit(mgr);
		} catch (Exception e) {
			rollback(mgr);
			throw new NwormQueryException("Nw.orm Exception", e);
		}
		return res;
	}

	@Override
	public int execQuery(String shql, QueryParameter... params) {
		EntityManager mgr = getEntityManager();
		int res = -1;
		try {
			Query query = mgr.createNativeQuery(shql);
			setParameters(query, params);
			
			res = query.executeUpdate();
			commit(mgr);
		} catch (Exception e) {
			rollback(mgr);
			throw new NwormQueryException("Nw.orm Exception", e);
		}
		return res;
	}

}
