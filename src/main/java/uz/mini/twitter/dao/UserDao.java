package uz.mini.twitter.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import uz.mini.twitter.entity.User;
import uz.mini.twitter.util.HibernateUtil;
import uz.mini.twitter.util.Passwords;

public class UserDao {

    /** Upgrade the previous demo's plaintext records before accepting HTTP traffic. */
    public void upgradeLegacyPasswords() {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            for (User user : em.createQuery("SELECT u FROM User u", User.class).getResultList()) {
                String stored = user.getPassword();
                if (!Boolean.TRUE.equals(user.getPasswordHashed())) {
                    user.setPassword(Passwords.hash(stored));
                    user.setPasswordHashed(true);
                }
            }
            em.getTransaction().commit();
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }

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
