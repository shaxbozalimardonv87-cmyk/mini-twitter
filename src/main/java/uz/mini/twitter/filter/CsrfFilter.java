package uz.mini.twitter.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Set;

public class CsrfFilter implements Filter {
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final Set<String> SAFE = Set.of("GET", "HEAD", "OPTIONS");

    public static void rotate(HttpSession session) {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        session.setAttribute("csrfToken", Base64.getUrlEncoder().withoutPadding().encodeToString(bytes));
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;
        req.setCharacterEncoding("UTF-8");
        HttpSession session = req.getSession(false);
        if (!SAFE.contains(req.getMethod())) {
            Object expected = session == null ? null : session.getAttribute("csrfToken");
            String supplied = req.getHeader("X-CSRF-Token");
            if (supplied == null) {
                String[] values = req.getParameterValues("_csrf");
                supplied = values != null && values.length == 1 ? values[0] : null;
            }
            if (!(expected instanceof String) || supplied == null || supplied.length() != 43
                    || !MessageDigest.isEqual(((String) expected).getBytes(StandardCharsets.UTF_8),
                    supplied.getBytes(StandardCharsets.UTF_8))) {
                resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid or missing CSRF token. Reload the form.");
                return;
            }
        } else {
            session = req.getSession(true);
            if (session.getAttribute("csrfToken") == null) rotate(session);
        }
        resp.setHeader("Cache-Control", "no-store");
        chain.doFilter(req, resp);
    }
}
