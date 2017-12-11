package nw.orm.hibernate;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.LockOptions;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.StatelessSession;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.hibernate.transform.Transformers;

import nw.orm.core.Entity;
import nw.orm.core.exception.NwormQueryException;
import nw.orm.core.query.QueryModifier;
import nw.orm.core.query.QueryParameter;

public class HibernateDao<T> extends HibernateDaoBase<T> {
	
	
	public HibernateDao(SessionFactory sxnFactory, Class<T> clazz) {
		super(sxnFactory, clazz, false, false);
	}
	
	public HibernateDao(SessionFactory sxnFactory, Class<T> clazz, boolean jtaEnabled, boolean useCurrentSession) {
		super(sxnFactory, clazz, jtaEnabled, useCurrentSession);
	}

	@Override
	public T getById(Serializable id) {
		T out = null;
		Session session = getSession();
		try {
			out = (T) session.get(entityClass, id, LockOptions.UPGRADE);
			commit(session);
		} catch (HibernateException e) {
			rollback(session);
			closeSession(session);
			throw new NwormQueryException("", e);
		}
		closeSession(session);
		return out;
	}

	@Override
	public T save(T item) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void delete(T item) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public T update(T item) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public T getByQuery(String query, QueryParameter... parameters) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<T> getAll() {
		return getListByCriteria();
	}
	
	public List<T> getListByCriteria(Criterion ... criteria) {
		return getListByCriteria(null, criteria);
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public List<T> getListByCriteria(QueryModifier qm, Criterion ... criteria){
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
				out = te.setResultTransformer(Transformers.aliasToBean(entityClass)).list();
			}
			commit(session);
		} catch (Exception e) {
			rollback(session);
			closeSession(session);
			throw new NwormQueryException("Nw.orm Exception", e);
		}
		closeSession(session);
		return out;
	}
	
	@Override
	public T getByCriteria(Criterion ... criteria) {
		return getByCriteria(null, criteria);
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public T getByCriteria(QueryModifier qm, Criterion ... criteria){
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
				out = (T) te.setResultTransformer(Transformers.aliasToBean(entityClass)).uniqueResult();
			}
			commit(session);
		} catch (Exception e) {
			rollback(session);
			closeSession(session);
			throw new NwormQueryException("", e);
		}
		closeSession(session);
		return out;
	}
	
	@Override
	public boolean softDelete(Serializable id) {
		if (!Entity.class.isAssignableFrom(entityClass)) {
			logger.debug("Unsupported class specified.");
			return false;
		}
		T bc = getByCriteria(Restrictions.idEq(id));
		if ((bc instanceof Entity)) {
			Entity e = (Entity) bc;
			e.setDeleted(true);
		}
		return update(bc) != null;
	}
	
	@Override
	public boolean bulkSoftDelete(List<Serializable> ids) {
		StatelessSession session = getStatelessSession();
		if (!Entity.class.isAssignableFrom(entityClass)) {
			logger.debug("Unsupported class specified.");
			return false;
		}

		try {
			for (Serializable s : ids) {
				Object entity = session.get(entityClass, s);
				Entity e = (Entity) entity;
				e.setDeleted(true);
				session.update(entity);
			}
			if(!jtaEnabled){
				session.getTransaction().commit();
			}
			session.close();
			return true;
		} catch (HibernateException e) {
			if(!jtaEnabled){
				session.getTransaction().rollback();
			}
			session.close();
			throw new NwormQueryException("Nw.orm Exception", e);
		}
	}
	

}
