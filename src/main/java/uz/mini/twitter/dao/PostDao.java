package uz.mini.twitter.dao;

import jakarta.persistence.EntityManager;
import uz.mini.twitter.entity.Post;
import uz.mini.twitter.util.HibernateUtil;

import java.util.List;

public class PostDao {

    public void save(Post post) {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(post);
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }

    public List<Post> findAllOrderByCreatedAtDesc() {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            return em.createQuery(
                            "SELECT p FROM Post p JOIN FETCH p.user ORDER BY p.createdAt DESC",
                            Post.class)
                    .getResultList();
        } finally {
            em.close();
        }
    }
    public List<Post> findByUserId(Long userId) {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            return em.createQuery(
                            "SELECT p FROM Post p WHERE p.user.id = :userId ORDER BY p.createdAt DESC",
                            Post.class)
                    .setParameter("userId", userId)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    public Post findById(Long id) {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            return em.find(Post.class, id);
        } finally {
            em.close();
        }
    }

    public void update(Post post) {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            em.merge(post);
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }

    public void deleteById(Long id) {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            Post post = em.find(Post.class, id);
            if (post != null) {
                em.remove(post);
            }
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }
}
