package uz.mini.twitter.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import uz.mini.twitter.dao.PostDao;
import uz.mini.twitter.entity.Post;
import uz.mini.twitter.entity.User;

import java.io.IOException;

@WebServlet("/edit-post")
public class EditPostServlet extends HttpServlet {

    private final PostDao postDao = new PostDao();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        Long postId = PostInput.id(req.getParameter("id"));
        if (postId == null) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        Post post = postDao.findById(postId);

        User currentUser = (User) req.getSession(false).getAttribute("user");

        if (post == null || !post.getUser().getId().equals(currentUser.getId())) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Bu post sizga tegishli emas.");
            return;
        }

        req.setAttribute("post", post);
        req.getRequestDispatcher("/edit-post.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        Long postId = PostInput.id(req.getParameter("id"));
        if (postId == null) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        String title = PostInput.clean(req.getParameter("title"));
        String description = PostInput.clean(req.getParameter("description"));

        Post post = postDao.findById(postId);
        User currentUser = (User) req.getSession(false).getAttribute("user");

        if (post == null || !post.getUser().getId().equals(currentUser.getId())) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Bu post sizga tegishli emas.");
            return;
        }

        if (!PostInput.valid(title, description)) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            req.setAttribute("post", post);
            req.setAttribute("error", PostInput.ERROR);
            req.getRequestDispatcher("/edit-post.jsp").forward(req, resp);
            return;
        }
        post.setTitle(title);
        post.setDescription(description);
        postDao.update(post);

        resp.sendRedirect(req.getContextPath() + "/my-posts");
    }
}
