package uz.mini.twitter.dao;

import org.junit.jupiter.api.Test;
import uz.mini.twitter.entity.Post;
import uz.mini.twitter.entity.User;
import uz.mini.twitter.util.HibernateUtil;
import uz.mini.twitter.util.Passwords;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class PostWorkflowTest {
    @Test
    void persistsListsEditsAndDeletesPostsWithDetachedSessionUser() {
        System.setProperty("DB_URL", "jdbc:h2:mem:twitter;MODE=PostgreSQL;DB_CLOSE_DELAY=-1");
        System.setProperty("DB_USERNAME", "sa");
        System.setProperty("DB_PASSWORD", "test");
        System.setProperty("DB_DRIVER", "org.h2.Driver");
        System.setProperty("DB_SCHEMA_ACTION", "create-drop");
        UserDao users = new UserDao();
        PostDao posts = new PostDao();
        User user = User.builder().fullName("Test Author").username("author").password("test-only").build();
        users.save(user);
        assertTrue(users.existsByUsername("author"));
        users.upgradeLegacyPasswords();
        String migrated = users.findByUsername("author").getPassword();
        assertNotEquals("test-only", migrated);
        assertTrue(Passwords.matches("test-only", migrated));
        assertTrue(users.findByUsername("author").getPasswordHashed());
        String prefixPassword = "pbkdf2-sha256$hello";
        User prefixUser = User.builder().fullName("Legacy").username("prefix")
                .password(prefixPassword).build();
        users.save(prefixUser);
        String hashShapedPassword = Passwords.hash("legacy-original");
        users.save(User.builder().fullName("Legacy formatted").username("formatted")
                .password(hashShapedPassword).build());
        users.upgradeLegacyPasswords();
        assertEquals(migrated, users.findByUsername("author").getPassword());
        assertTrue(Passwords.matches(prefixPassword, users.findByUsername("prefix").getPassword()));
        assertTrue(Passwords.matches(hashShapedPassword, users.findByUsername("formatted").getPassword()));
        String migratedPrefix = users.findByUsername("prefix").getPassword();
        users.upgradeLegacyPasswords();
        assertEquals(migratedPrefix, users.findByUsername("prefix").getPassword());
        Post post = Post.builder().title("Salom").description("O‘zbekcha matn")
                .createdAt(LocalDateTime.now()).user(user).build();
        posts.save(post);
        assertNotNull(post.getId());
        assertEquals(1, posts.findAllOrderByCreatedAtDesc().size());
        assertEquals(1, posts.findByUserId(user.getId()).size());
        Post loaded = posts.findById(post.getId());
        // This must work after the DAO has closed its EntityManager.
        assertEquals("author", loaded.getUser().getUsername());
        loaded.setTitle("Updated");
        posts.update(loaded);
        assertEquals("Updated", posts.findById(post.getId()).getTitle());
        posts.deleteById(post.getId());
        assertNull(posts.findById(post.getId()));
        assertTrue(posts.findByUserId(user.getId()).isEmpty());
        HibernateUtil.shutdown();
    }
}
