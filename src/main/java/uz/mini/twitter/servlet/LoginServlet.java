package uz.mini.twitter.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import uz.mini.twitter.dao.UserDao;
import uz.mini.twitter.entity.User;
import uz.mini.twitter.util.Passwords;
import uz.mini.twitter.filter.CsrfFilter;

import java.io.IOException;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    private final UserDao userDao = new UserDao();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.getRequestDispatcher("/login.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String username = req.getParameter("username");
        String password = req.getParameter("password");

        User user = userDao.findByUsername(username);

        if (user == null || !Boolean.TRUE.equals(user.getPasswordHashed())
                || !Passwords.matches(password, user.getPassword())) {
            req.setAttribute("error", "Username yoki parol xato.");
            req.getRequestDispatcher("/login.jsp").forward(req, resp);
            return;
        }

        HttpSession session = req.getSession(true);
        req.changeSessionId();
        CsrfFilter.rotate(session);
        user.setPassword(null);
        session.setAttribute("user", user);

        resp.sendRedirect(req.getContextPath() + "/home");
    }
}
