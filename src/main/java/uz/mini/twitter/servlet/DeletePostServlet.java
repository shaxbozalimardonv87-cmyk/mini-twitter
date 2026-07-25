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

@WebServlet("/delete-post")
public class DeletePostServlet extends HttpServlet {

    private final PostDao postDao = new PostDao();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        Long postId = Long.valueOf(req.getParameter("id"));
        Post post = postDao.findById(postId);

        User currentUser = (User) req.getSession(false).getAttribute("user");

        if (post == null || !post.getUser().getId().equals(currentUser.getId())) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Bu post sizga tegishli emas.");
            return;
        }

        postDao.deleteById(postId);
        resp.sendRedirect(req.getContextPath() + "/my-posts");
    }
}
