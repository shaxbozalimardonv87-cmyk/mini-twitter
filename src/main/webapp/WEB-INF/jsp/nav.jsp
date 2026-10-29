<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<nav style="display:flex; gap:16px; padding:12px 20px; background:#15202b; align-items:center;">
    <a href="${pageContext.request.contextPath}/home" style="color:#1d9bf0; font-weight:bold; text-decoration:none;">Mini Twitter</a>
    <a href="${pageContext.request.contextPath}/home" style="color:#fff; text-decoration:none;">Home</a>

    <c:if test="${empty sessionScope.user}">
        <a href="${pageContext.request.contextPath}/login" style="color:#fff; text-decoration:none;">Login</a>
        <a href="${pageContext.request.contextPath}/register" style="color:#fff; text-decoration:none;">Register</a>
    </c:if>

    <c:if test="${not empty sessionScope.user}">
        <a href="${pageContext.request.contextPath}/add-post" style="color:#fff; text-decoration:none;">Add Post</a>
        <a href="${pageContext.request.contextPath}/my-posts" style="color:#fff; text-decoration:none;">My Posts</a>
        <span style="margin-left:auto; color:#8899a6;">Hi, <c:out value="${sessionScope.user.fullName}"/></span>
        <a href="${pageContext.request.contextPath}/logout" style="color:#fff; text-decoration:none;">Logout</a>
    </c:if>
</nav>
