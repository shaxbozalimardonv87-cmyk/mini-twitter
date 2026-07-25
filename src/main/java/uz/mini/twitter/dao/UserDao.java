package uz.mini.twitter.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import uz.mini.twitter.entity.User;
import uz.mini.twitter.util.HibernateUtil;

public class UserDao {

    public void save(User user) {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(user);
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }

    public User findByUsername(String username) {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            return em.createQuery(
                            "SELECT u FROM User u WHERE u.username = :username", User.class)
                    .setParameter("username", username)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        } finally {
            em.close();
        }
    }

    public boolean existsByUsername(String username) {
        return findByUsername(username) != null;
    }

    public User findById(Long id) {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            return em.find(User.class, id);
        } finally {
            em.close();
        }
    }
}
