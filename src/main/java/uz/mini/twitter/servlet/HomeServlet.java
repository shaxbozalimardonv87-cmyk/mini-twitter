package uz.mini.twitter.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import uz.mini.twitter.dao.PostDao;
import uz.mini.twitter.entity.Post;

import java.io.IOException;
import java.util.List;

@WebServlet({"/", "/home"})
public class HomeServlet extends HttpServlet {

    private final PostDao postDao = new PostDao();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        List<Post> posts = postDao.findAllOrderByCreatedAtDesc();
        req.setAttribute("posts", posts);
        req.getRequestDispatcher("/home.jsp").forward(req, resp);
    }
}
