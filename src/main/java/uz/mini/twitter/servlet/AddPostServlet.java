package uz.mini.twitter.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import uz.mini.twitter.dao.PostDao;
import uz.mini.twitter.entity.Post;
import uz.mini.twitter.entity.User;

import java.io.IOException;
import java.time.LocalDateTime;

@WebServlet("/add-post")
public class AddPostServlet extends HttpServlet {
    private final PostDao postDao;

    public AddPostServlet() {
        this(new PostDao());
    }

    AddPostServlet(PostDao postDao) {
        this.postDao = postDao;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.getRequestDispatcher("/add-post.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String title = PostInput.clean(req.getParameter("title"));
        String description = PostInput.clean(req.getParameter("description"));
        req.setAttribute("title", title);
        req.setAttribute("description", description);
        if (!PostInput.valid(title, description)) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            req.setAttribute("error", PostInput.ERROR);
            req.getRequestDispatcher("/add-post.jsp").forward(req, resp);
            return;
        }

        // The authentication filter supplies the signed-in user, never a submitted user ID.
        User currentUser = (User) req.getSession(false).getAttribute("user");
        Post post = Post.builder().title(title).description(description)
                .createdAt(LocalDateTime.now()).user(currentUser).build();
        postDao.save(post);
        resp.sendRedirect(req.getContextPath() + "/my-posts");
    }
}
