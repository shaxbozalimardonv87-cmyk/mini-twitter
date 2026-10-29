package uz.mini.twitter.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import uz.mini.twitter.dao.UserDao;
import uz.mini.twitter.entity.User;
import uz.mini.twitter.util.Passwords;

import java.io.IOException;

@WebServlet("/register")
public class RegisterServlet extends HttpServlet {

    private final UserDao userDao = new UserDao();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.getRequestDispatcher("/register.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String fullName = PostInput.clean(req.getParameter("fullName"));
        String username = PostInput.clean(req.getParameter("username"));
        String password = req.getParameter("password");

        if (fullName == null || fullName.isBlank()
                || username == null || username.isBlank()
                || password == null || password.isBlank()
                || fullName.length() > 255 || username.length() > 255 || password.length() > 255) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            req.setAttribute("error", "Barcha maydonlarni to'ldiring.");
            req.getRequestDispatcher("/register.jsp").forward(req, resp);
            return;
        }

        if (userDao.existsByUsername(username)) {
            req.setAttribute("error", "Bu username band, boshqasini tanlang.");
            req.getRequestDispatcher("/register.jsp").forward(req, resp);
            return;
        }

        User user = User.builder().fullName(fullName.strip())
                .username(username.strip()).password(Passwords.hash(password)).passwordHashed(true).build();
        userDao.save(user);

        resp.sendRedirect(req.getContextPath() + "/login");
    }
}
