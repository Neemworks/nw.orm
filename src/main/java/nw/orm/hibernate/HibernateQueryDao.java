package nw.orm.hibernate;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Criterion;
import org.hibernate.transform.Transformers;

import nw.orm.core.exception.NwormQueryException;
import nw.orm.core.query.QueryModifier;
import nw.orm.core.query.QueryParameter;
import nw.orm.core.query.SQLModifier;
import nw.orm.dao.QueryDao;

public class HibernateQueryDao extends HibernateDaoBase implements QueryDao {

	public HibernateQueryDao(SessionFactory sxnFactory, boolean jtaEnabled, boolean useCurrentSession) {
		super(sxnFactory, jtaEnabled, useCurrentSession);
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public <T> T query(Class<T> resultClass, String queryString, QueryParameter ... parameters) {
		T out = null;
		
		Session session = getSession();
		try {

			Query query = session.createQuery(queryString);
			for (QueryParameter rp : parameters) {
				query.setParameter(rp.getName(), rp.getValue());
			}
			
			try {
				isClassMapped(resultClass);
				out = (T) query.uniqueResult();
			} catch (Exception e) {
				out = (T) query.setResultTransformer(Transformers.aliasToBean(resultClass)).uniqueResult();
			}
			
			commit(session);
		} catch (HibernateException e) {
			rollback(session);
			throw new NwormQueryException("Nw.orm Exception", e);
		}
		
		return out;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public <T> List<T> queryList(Class<T> resultClass, String queryString, QueryParameter ... parameters) {
		List<T> out = new ArrayList<T>();
		Session session = getSession();
		try {

			Query query = session.createQuery(queryString);
			for (QueryParameter rp : parameters) {
				query.setParameter(rp.getName(), rp.getValue());
			}

			try {
				isClassMapped(resultClass);
				out = query.list();
			} catch (Exception e) {
				out = query.setResultTransformer(Transformers.aliasToBean(resultClass)).list();
			}

			commit(session);
		} catch (HibernateException e) {
			rollback(session);
			throw new NwormQueryException("Nw.orm Exception", e);
		}
		
		return out;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public <T> T get(Class<T> returnClazz, QueryModifier qm, Criterion ... criteria){
		
		T out = null;
		Session session = getSession();
		try {
			Criteria te = session.createCriteria(qm.getQueryClazz());
			for (Criterion c : criteria) {
				te.add(c);
			}
			modifyCriteria(te, qm);
			if(!qm.isTransformResult()){
				out = (T) te.uniqueResult();
			}else{
				out = (T) te.setResultTransformer(Transformers.aliasToBean(returnClazz)).uniqueResult();
			}
			commit(session);
		} catch (Exception e) {
			rollback(session);
			throw new NwormQueryException("Nw.orm Exception", e);
		}
		
		return out;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public <T> List<T> list(Class<T> returnClazz, QueryModifier qm, Criterion ... criteria){
		List<T> out = new ArrayList<T>();
		Session session = getSession();
		try {
			Criteria te = session.createCriteria(qm.getQueryClazz());
			for (Criterion c : criteria) {
				te.add(c);
			}
			modifyCriteria(te, qm);
			if(!qm.isTransformResult()){
				out = te.list();
			}else{
				out = te.setResultTransformer(Transformers.aliasToBean(returnClazz)).list();
			}
			commit(session);
		} catch (Exception e) {
			rollback(session);
			throw new NwormQueryException("", e);
		}
		
		return out;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> List<T> getBySQL(Class<T> returnClazz, String sql, SQLModifier sqlMod, QueryParameter... params) {
		List<T> out = new ArrayList<T>();
		Session session = getSession();
		SQLQuery te = session.createSQLQuery(sql);

		if (params != null) {
			for (QueryParameter param : params) {
				te.setParameter(param.getName(), param.getValue());
			}
		}

		if(sqlMod != null){
			
			try {
				isClassMapped(returnClazz);
				te.addEntity(returnClazz);
			} catch (Exception e) {
				te.setResultTransformer(Transformers.aliasToBean(returnClazz));
			}
//			if(Entity.class.isAssignableFrom(returnClazz)){
//				te.setParameter("deleted", false);
//			}
			if(sqlMod.isPaginated()){
				te.setFirstResult(sqlMod.getPageIndex());
				te.setMaxResults(sqlMod.getMaxResult());
			}
		}

		try {
			out = te.list();
		} catch (Exception e) {
			rollback(session);
			throw new NwormQueryException("Exception", e);
		}
		commit(session);
		return out;
	}

	



}
