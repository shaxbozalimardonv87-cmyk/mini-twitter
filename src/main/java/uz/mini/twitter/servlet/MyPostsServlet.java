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
import java.util.List;

@WebServlet("/my-posts")
public class MyPostsServlet extends HttpServlet {

    private final PostDao postDao = new PostDao();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession session = req.getSession(false);
        User currentUser = (User) session.getAttribute("user");

        List<Post> myPosts = postDao.findByUserId(currentUser.getId());
        req.setAttribute("posts", myPosts);
        req.getRequestDispatcher("/my-posts.jsp").forward(req, resp);
    }
}
