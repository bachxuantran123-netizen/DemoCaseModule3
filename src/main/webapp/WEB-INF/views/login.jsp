<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <title>Đăng nhập hệ thống</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.0/font/bootstrap-icons.css">
    <link href="${pageContext.request.contextPath}/css/style.css" rel="stylesheet">
</head>
<body class="d-flex justify-content-center align-items-center login-page">

<div class="card login-card shadow-lg p-4" style="width: 400px; background: rgba(255, 255, 255, 0.95);">
    <div class="card-body">
        <div class="text-center mb-4">
            <div class="bg-primary text-white rounded-circle d-inline-flex align-items-center justify-content-center mb-3" style="width: 60px; height: 60px;">
                <i class="bi bi-bank2 fs-2"></i>
            </div>
            <h4 class="fw-bold text-secondary">E-BANKING LOGIN</h4>
            <p class="text-muted small">Vui lòng đăng nhập để tiếp tục</p>
        </div>

        <% String error = (String) request.getAttribute("error"); %>
        <% if (error != null) { %>
        <div class="alert alert-danger d-flex align-items-center" role="alert">
            <i class="bi bi-exclamation-triangle-fill me-2"></i>
            <div><%= error %></div>
        </div>
        <% } %>

        <form action="login" method="post">
            <div class="form-floating mb-3">
                <input type="text" name="username" class="form-control" id="floatingInput" placeholder="Tên đăng nhập" required>
                <label for="floatingInput"><i class="bi bi-person"></i> Tên đăng nhập</label>
            </div>

            <div class="form-floating mb-4">
                <input type="password" name="password" class="form-control" id="floatingPassword" placeholder="Mật khẩu" required>
                <label for="floatingPassword"><i class="bi bi-lock"></i> Mật khẩu</label>
            </div>

            <button type="submit" class="btn btn-primary btn-login w-100 py-2 fw-bold text-white rounded-pill">
                ĐĂNG NHẬP <i class="bi bi-arrow-right-short"></i>
            </button>
        </form>
    </div>
    <div class="card-footer bg-transparent border-0 text-center mt-2">
        <small class="text-muted">Module 3 Demo Case &copy; 2023</small>
    </div>
</div>

</body>
</html>