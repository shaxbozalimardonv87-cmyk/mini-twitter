package uz.mini.twitter.servlet;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.*;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import uz.mini.twitter.dao.PostDao;
import uz.mini.twitter.entity.Post;
import uz.mini.twitter.entity.User;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AddPostServletTest {
    @Test
    void createsPostForSessionUserAndRedirectsAfterSaving() throws Exception {
        PostDao dao = mock(PostDao.class);
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);
        User user = User.builder().id(42L).username("author").build();
        when(req.getSession(false)).thenReturn(session);
        when(session.getAttribute("user")).thenReturn(user);
        when(req.getContextPath()).thenReturn("/mini-twitter");
        when(req.getParameter("title")).thenReturn("  Salom  ");
        when(req.getParameter("description")).thenReturn("  O‘zbekcha post  ");
        new AddPostServlet(dao).doPost(req, resp);
        ArgumentCaptor<Post> saved = ArgumentCaptor.forClass(Post.class);
        verify(dao).save(saved.capture());
        assertEquals("Salom", saved.getValue().getTitle());
        assertEquals("O‘zbekcha post", saved.getValue().getDescription());
        assertSame(user, saved.getValue().getUser());
        assertNotNull(saved.getValue().getCreatedAt());
        verify(resp).sendRedirect("/mini-twitter/my-posts");
    }

    @Test
    void rejectsEmptyAndOversizedInputWithoutSaving() throws Exception {
        for (String title : new String[]{null, "  ", "x".repeat(256)}) {
            PostDao dao = mock(PostDao.class);
            HttpServletRequest req = mock(HttpServletRequest.class);
            HttpServletResponse resp = mock(HttpServletResponse.class);
            RequestDispatcher dispatcher = mock(RequestDispatcher.class);
            when(req.getParameter("title")).thenReturn(title);
            when(req.getParameter("description")).thenReturn("body");
            when(req.getRequestDispatcher("/add-post.jsp")).thenReturn(dispatcher);
            new AddPostServlet(dao).doPost(req, resp);
            verifyNoInteractions(dao);
            verify(resp).setStatus(400);
            verify(dispatcher).forward(req, resp);
        }
    }

    @Test
    void displaysCreationForm() throws Exception {
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        RequestDispatcher dispatcher = mock(RequestDispatcher.class);
        when(req.getRequestDispatcher("/add-post.jsp")).thenReturn(dispatcher);
        new AddPostServlet(mock(PostDao.class)).doGet(req, resp);
        verify(dispatcher).forward(req, resp);
    }

    @Test
    void validatesDescriptionAndIds() {
        assertFalse(PostInput.valid("title", ""));
        assertFalse(PostInput.valid("title", "x".repeat(256)));
        assertTrue(PostInput.valid("x".repeat(255), "y".repeat(255)));
        assertNull(PostInput.id(null));
        assertNull(PostInput.id("abc"));
        assertNull(PostInput.id("-1"));
        assertEquals(1L, PostInput.id("1"));
    }
}
