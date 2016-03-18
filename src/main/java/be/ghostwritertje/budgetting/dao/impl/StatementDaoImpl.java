package be.ghostwritertje.budgetting.dao.impl;

import be.ghostwritertje.budgetting.dao.HibernateUtil;
import be.ghostwritertje.budgetting.dao.api.StatementDao;
import be.ghostwritertje.budgetting.domain.Rekening;
import be.ghostwritertje.budgetting.domain.Statement;
import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by jorandeboever
 * on 16/03/16.
 */
@Repository
public class StatementDaoImpl implements StatementDao {

    private SessionFactory sessionFactory = HibernateUtil.getSessionFactory();

    @Override
    public List<Statement> getStatements(Rekening rekening) {

        Transaction transaction = sessionFactory.getCurrentSession().beginTransaction();

        Query query = sessionFactory.getCurrentSession().createQuery("from Statement s where s.aankomstRekening.nummer = :aankomstRekeningNummer");
        query.setParameter("aankomstRekeningNummer", rekening.getNummer());
        List<Statement> statements = query.list();

        query = sessionFactory.getCurrentSession().createQuery("from Statement s where s.vertrekRekening.nummer = :aankomstRekeningNummer");
        query.setParameter("aankomstRekeningNummer", rekening.getNummer());
        List<Statement> otherStatements = query.list();

        for (Statement statement : otherStatements) {
            statement.setBedrag(-Math.abs(statement.getBedrag()));
            statements.add(statement);
        }
        transaction.commit();
        return statements;
    }

    @Override
    public void createStatement(Statement statement) {
        Transaction transaction = sessionFactory.getCurrentSession().beginTransaction();
        try {
            if(statement.getAankomstRekening() != null && statement.getAankomstRekening().getUser() != null ) {
                sessionFactory.getCurrentSession().saveOrUpdate(statement.getAankomstRekening().getUser());
                sessionFactory.getCurrentSession().saveOrUpdate(statement.getAankomstRekening());
            }
            if(statement.getVertrekRekening() != null && statement.getVertrekRekening().getUser() != null ) {
                sessionFactory.getCurrentSession().saveOrUpdate(statement.getVertrekRekening().getUser());
                sessionFactory.getCurrentSession().saveOrUpdate(statement.getVertrekRekening());
            }
            sessionFactory.getCurrentSession().saveOrUpdate(statement);
            transaction.commit();
        } catch (ConstraintViolationException e) {
            transaction.rollback();
        }
    }

    @Override
    public void deleteAllStatements() {
        Transaction transaction = sessionFactory.getCurrentSession().beginTransaction();

        Query query = sessionFactory.getCurrentSession().createQuery("delete from Statement r");
        query.executeUpdate();

        transaction.commit();
    }


}
