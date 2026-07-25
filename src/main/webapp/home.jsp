<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="uz">
<head>
    <meta charset="UTF-8">
    <title>Mini Twitter - Home</title>
    <style>
        body { font-family: Arial, sans-serif; background:#000; color:#e7e9ea; margin:0; }
        .container { max-width:600px; margin:0 auto; }
        .post { border-bottom:1px solid #2f3336; padding:16px; }
        .post-title { font-weight:bold; font-size:18px; margin:0 0 6px; }
        .post-desc { margin:0 0 8px; white-space:pre-wrap; }
        .post-meta { color:#8899a6; font-size:13px; }
        .empty { padding:20px; color:#8899a6; }
    </style>
</head>
<body>
<jsp:include page="/WEB-INF/jsp/nav.jsp"/>

<div class="container">
    <c:if test="${empty posts}">
        <div class="empty">Hozircha postlar yo'q.</div>
    </c:if>

    <c:forEach var="post" items="${posts}">
        <div class="post">
            <p class="post-title">${post.title}</p>
            <p class="post-desc">${post.description}</p>
            <p class="post-meta">
                @${post.user.username} (${post.user.fullName}) &middot; ${post.createdAt}
            </p>
        </div>
    </c:forEach>
</div>
</body>
</html>
