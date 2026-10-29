<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="uz">
<head>
    <meta charset="UTF-8">
    <title>Edit Post - Mini Twitter</title>
    <style>
        body { font-family: Arial, sans-serif; background:#000; color:#e7e9ea; margin:0; }
        .form-box { max-width:500px; margin:40px auto; padding:24px; border:1px solid #2f3336; border-radius:12px; }
        input, textarea { width:100%; padding:10px; margin-bottom:12px; box-sizing:border-box; background:#000; color:#fff; border:1px solid #2f3336; border-radius:6px; font-family:inherit; }
        textarea { min-height:120px; resize:vertical; }
        button { width:100%; padding:10px; background:#1d9bf0; color:#fff; border:none; border-radius:20px; font-weight:bold; cursor:pointer; }
        h2 { text-align:center; }
    </style>
</head>
<body>
<jsp:include page="/WEB-INF/jsp/nav.jsp"/>

<div class="form-box">
    <h2>Postni tahrirlash</h2>
    <c:if test="${not empty error}"><p role="alert"><c:out value="${error}"/></p></c:if>
    <form method="post" action="${pageContext.request.contextPath}/edit-post?id=${post.id}">
        <input type="hidden" name="_csrf" value="${sessionScope.csrfToken}">
        <input type="text" name="title" maxlength="255" value="<c:out value='${post.title}'/>" required>
        <textarea name="description" maxlength="255" required><c:out value="${post.description}"/></textarea>
        <button type="submit">Saqlash</button>
    </form>
</div>
</body>
</html>
