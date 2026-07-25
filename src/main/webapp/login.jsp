<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="uz">
<head>
    <meta charset="UTF-8">
    <title>Login - Mini Twitter</title>
    <style>
        body { font-family: Arial, sans-serif; background:#000; color:#e7e9ea; margin:0; }
        .form-box { max-width:360px; margin:60px auto; padding:24px; border:1px solid #2f3336; border-radius:12px; }
        input { width:100%; padding:10px; margin-bottom:12px; box-sizing:border-box; background:#000; color:#fff; border:1px solid #2f3336; border-radius:6px; }
        button { width:100%; padding:10px; background:#1d9bf0; color:#fff; border:none; border-radius:20px; font-weight:bold; cursor:pointer; }
        .error { color:#f4212e; margin-bottom:12px; }
        h2 { text-align:center; }
        .link { text-align:center; margin-top:12px; }
        a { color:#1d9bf0; }
    </style>
</head>
<body>
<jsp:include page="/WEB-INF/jsp/nav.jsp"/>

<div class="form-box">
    <h2>Login</h2>
    <c:if test="${not empty error}">
        <div class="error">${error}</div>
    </c:if>
    <form method="post" action="${pageContext.request.contextPath}/login">
        <input type="text" name="username" placeholder="Username" required>
        <input type="password" name="password" placeholder="Password" required>
        <button type="submit">Login</button>
    </form>
    <div class="link">Akkaunt yo'q? <a href="${pageContext.request.contextPath}/register">Register</a></div>
</div>
</body>
</html>
