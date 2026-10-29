<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="uz">
<head>
    <meta charset="UTF-8">
    <title>My Posts - Mini Twitter</title>
    <style>
        body { font-family: Arial, sans-serif; background:#000; color:#e7e9ea; margin:0; }
        .container { max-width:600px; margin:0 auto; }
        .post { border-bottom:1px solid #2f3336; padding:16px; }
        .post-title { font-weight:bold; font-size:18px; margin:0 0 6px; }
        .post-desc { margin:0 0 8px; white-space:pre-wrap; }
        .post-meta { color:#8899a6; font-size:13px; margin-bottom:8px; }
        .actions a { margin-right:12px; color:#1d9bf0; text-decoration:none; font-size:14px; }
        .empty { padding:20px; color:#8899a6; }
        h2 { padding:16px; margin:0; }
    </style>
</head>
<body>
<jsp:include page="/WEB-INF/jsp/nav.jsp"/>

<div class="container">
    <h2>Mening postlarim</h2>

    <c:if test="${empty posts}">
        <div class="empty">Sizda hali post yo'q.</div>
    </c:if>

    <c:forEach var="post" items="${posts}">
        <div class="post">
            <p class="post-title"><c:out value="${post.title}"/></p>
            <p class="post-desc"><c:out value="${post.description}"/></p>
            <p class="post-meta">${post.createdAt}</p>
            <div class="actions">
                <a href="${pageContext.request.contextPath}/edit-post?id=${post.id}">Edit</a>
                <form method="post" action="${pageContext.request.contextPath}/delete-post" style="display:inline;"
                      onsubmit="return confirm('Postni oʻchirishni tasdiqlaysizmi?');">
                    <input type="hidden" name="id" value="${post.id}">
                    <input type="hidden" name="_csrf" value="${sessionScope.csrfToken}">
                    <button type="submit">Delete</button>
                </form>
            </div>
        </div>
    </c:forEach>
</div>
</body>
</html>
