package nw.orm.core.session;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;

import org.hibernate.FlushMode;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.StatelessSession;

import nw.commons.NeemClazz;

/**
 * An entry point for manipulating hibernate sessions and session factory.
 *
 * The default configuration disables use of container managed sessions (current session) while enabling user managed transactions.
 * Nw.orm still has the ability to manage opening and closing of sessions implicitly.
 *
 * @author Ogwara O. Rowland
 */
public class HibernateSessionService extends NeemClazz implements IHibernateSessionService{

	/** The Hibernate Session Factory reference */
	private HibernateSessionFactory conf;

	/** Default flush mode. */
	private FlushMode flushMode = FlushMode.COMMIT;

	/** Configures the system use currentSession instead of opening a new session each time.
	 * true uses currentSession  bound to context
	 *
	 */
	private boolean useCurrentSession = false;

	/** Whether to use JTA or Local transactions
	 * true for Local transactions, false for JTA based transactions
	 */
	private boolean useTransactions = true;

	private String userTransactionJNDI = "java:comp/UserTransaction";

	/**
	 * Instantiates a new hibernate session service.
	 *
	 * @param conf Hibernate Session Factory
	 */
	public HibernateSessionService(HibernateSessionFactory conf) {
		this.conf = conf;
	}

	/* (non-Javadoc)
	 * @see nw.orm.core.session.IHibernateSessionService#getManagedSession()
	 */
	@Override
	public Session getManagedSession() {
		if(useCurrentSession){
			return getCurrentSession();
		}
		return getRawSession();
	}

	/* (non-Javadoc)
	 * @see nw.orm.core.session.IHibernateSessionService#getRawSession()
	 */
	@Override
	public Session getRawSession() {
		SessionFactory sf = conf.getSessionFactory();
		Session sxn = sf.openSession();
		sxn.setFlushMode(flushMode);
		beginTransaction(sxn);
		return sxn;
	}

	/* (non-Javadoc)
	 * @see nw.orm.core.session.IHibernateSessionService#getCurrentSession()
	 */
	@Override
	public Session getCurrentSession() {
		SessionFactory sf = conf.getSessionFactory();
		Session sxn = sf.getCurrentSession();
		sxn.setFlushMode(flushMode);
		beginTransaction(sxn);
		return sxn;
	}

	/* (non-Javadoc)
	 * @see nw.orm.core.session.IHibernateSessionService#closeSession(org.hibernate.Session)
	 */
	@Override
	public void closeSession(Session sxn) {
		if ((sxn != null) && (!this.useCurrentSession)){
			sxn.close();
		}
	}

	/* (non-Javadoc)
	 * @see nw.orm.core.session.IHibernateSessionService#commit(org.hibernate.Session)
	 */
	@Override
	public void commit(Session sxn) throws HibernateException{
		logger.trace("Commit in progress ");
		if(useTransactions()){
			sxn.getTransaction().commit();
		}else{
			try {
				getUserTransaction().commit();
			} catch (SystemException e) {
				logger.error("Exception ", e);
			} catch (NamingException e) {
				logger.error("Exception ", e);
			} catch (SecurityException e) {
				logger.error("Exception ", e);
			} catch (IllegalStateException e) {
				logger.error("Exception ", e);
			} catch (RollbackException e) {
				logger.error("Exception ", e);
			} catch (HeuristicMixedException e) {
				logger.error("Exception ", e);
			} catch (HeuristicRollbackException e) {
				logger.error("Exception ", e);
			}
		}
	}

	/* (non-Javadoc)
	 * @see nw.orm.core.session.IHibernateSessionService#rollback(org.hibernate.Session)
	 */
	@Override
	public void rollback(Session sxn) throws HibernateException{
		logger.trace("Rollback in progress ");
		if(useTransactions()){
			sxn.getTransaction().rollback();
		}else{
			try {
				getUserTransaction().rollback();
			} catch (SystemException e) {
				logger.error("Exception ", e);
			} catch (NamingException e) {
				logger.error("Exception ", e);
			}
		}
	}

	/* (non-Javadoc)
	 * @see nw.orm.core.session.IHibernateSessionService#getStatelessSession()
	 */
	@Override
	public StatelessSession getStatelessSession() {
		SessionFactory sf = conf.getSessionFactory();
		StatelessSession ss = sf.openStatelessSession();
		if(useTransactions()){
			ss.beginTransaction();
		}else{
			try {
				getUserTransaction().begin();
			} catch (NotSupportedException e) {
				logger.error("Exception ", e);
			} catch (SystemException e) {
				logger.error("Exception ", e);
			} catch (NamingException e) {
				logger.error("Exception ", e);
			}
		}
		return ss;
	}

	/* (non-Javadoc)
	 * @see nw.orm.core.session.IHibernateSessionService#getFactory()
	 */
	@Override
	public SessionFactory getFactory() {
		return conf.getSessionFactory();
	}

	/**
	 * Begin transaction.
	 *
	 * @param sxn the sxn
	 */
	private void beginTransaction(Session sxn){
		if(useTransactions()){
			sxn.beginTransaction();
		}else{
			try {
				getUserTransaction().begin();
			} catch (NotSupportedException e) {
				logger.error("Exception ", e);
			} catch (SystemException e) {
				logger.error("Exception ", e);
			} catch (NamingException e) {
				logger.error("Exception ", e);
			}
		}
	}

	/**
	 * Enable current session.
	 */
	public void enableCurrentSession(){
		this.useCurrentSession = true;
	}

	/**
	 * Enable transactions.
	 */
	public void enableTransactions(){
		this.useTransactions = true;
	}

	/**
	 * Disable current session.
	 */
	public void disableCurrentSession(){
		this.useCurrentSession = false;
	}

	/**
	 * Disable transactions.
	 */
	public void disableTransactions(){
		this.useTransactions = false;
	}

	/**
	 * Use transactions.
	 *
	 * @return true, if successful
	 */
	public boolean useTransactions() {
		return useTransactions;
	}

	/**
	 * Retrieves the current user transaction
	 * @return UserTransaction
	 * @throws NamingException if name was not found in context
	 */
	public UserTransaction getUserTransaction() throws NamingException{
		UserTransaction utx = (UserTransaction) new InitialContext().lookup(getUserTransactionJNDI());

		return utx;
	}

	/**
	 *
	 * @return userTransaction JNDI
	 */
	public String getUserTransactionJNDI() {
		return userTransactionJNDI;
	}

	/**
	 * Specifies the jndi for userTransactions
	 * @param userTransactionJNDI
	 */
	public void setUserTransactionJNDI(String userTransactionJNDI) {
		this.userTransactionJNDI = userTransactionJNDI;
	}

}
