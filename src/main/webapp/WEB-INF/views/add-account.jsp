<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<!DOCTYPE html>
<html>
<head>
    <title>${account != null ? 'Cập Nhật Tài Khoản' : 'Thêm Tài Khoản Mới'}</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/css/style.css" rel="stylesheet">
</head>
<body class="bg-light">
<div class="container mt-5">
    <div class="card shadow mx-auto" style="max-width: 700px;">
        <div class="card-header ${account != null ? 'bg-warning text-dark' : 'bg-success text-white'}">
            <h4 class="mb-0">
                <c:if test="${account != null}">Cập Nhật: ${account.accountCode}</c:if>
                <c:if test="${account == null}">Thêm Tài Khoản Mới</c:if>
            </h4>
        </div>
        <div class="card-body">

            <c:if test="${not empty error}">
                <div class="alert alert-danger">${error}</div>
            </c:if>

            <form action="account" method="post">
                <input type="hidden" name="action" value="${account != null ? 'update' : 'insert'}">

                <div class="row mb-3">
                    <div class="col">
                        <label class="form-label fw-bold">Mã Tài Khoản</label>
                        <input type="text" name="code" class="form-control"
                               value="${account.accountCode}"
                               required pattern="\d{9,12}" title="Nhập mã số (9-12 ký tự)"
                        ${account != null ? 'readonly style="background-color: #e9ecef;"' : ''}>
                    </div>
                    <div class="col">
                        <label class="form-label fw-bold">Số CCCD</label>
                        <input type="text" name="citizenId" class="form-control"
                               value="${account.citizenId}" required>
                    </div>
                </div>

                <div class="mb-3">
                    <label class="form-label fw-bold">Họ và Tên</label>
                    <input type="text" name="name" class="form-control"
                           value="${account.ownerName}" required>
                </div>

                <div class="mb-3">
                    <label class="form-label fw-bold">Loại Tài Khoản</label>

                    <c:if test="${account != null}">
                        <input type="hidden" name="type" value="${account.accountType == 'SavingsAccount' ? 'SAVINGS' : 'PAYMENT'}">
                    </c:if>

                    <select id="typeSelect" name="type" class="form-select" onchange="toggleFields()"
                    ${account != null ? 'disabled' : ''}>
                        <option value="SAVINGS" ${account.accountType == 'SavingsAccount' ? 'selected' : ''}>
                            Tài khoản Tiết kiệm
                        </option>
                        <option value="PAYMENT" ${account.accountType == 'PaymentAccount' ? 'selected' : ''}>
                            Tài khoản Thanh toán
                        </option>
                    </select>
                </div>

                <div id="savingsFields" class="p-3 border rounded bg-white mb-3" style="display: block;">
                    <h6 class="text-success border-bottom pb-2">Thông tin Tiết Kiệm</h6>
                    <div class="row mb-3">
                        <div class="col">
                            <label>Số tiền gửi</label>
                            <input type="number" name="depositAmount" class="form-control"
                                   value="<fmt:formatNumber value="${account.accountType == 'SavingsAccount' ? account.depositAmount : 0}" pattern="#0"/>">
                        </div>
                        <div class="col">
                            <label>Ngày gửi (yyyy-MM-dd)</label>
                            <input type="date" name="depositDate" id="depositDate" class="form-control"
                                   value="${account.accountType == 'SavingsAccount' ? account.depositDate : java.time.LocalDate.now()}">
                        </div>
                    </div>
                    <div class="row mb-3">
                        <div class="col">
                            <label>Lãi suất (%)</label>
                            <input type="number" step="0.1" name="interestRate" class="form-control"
                                   value="${account.accountType == 'SavingsAccount' ? account.interestRate : 0}">
                        </div>
                        <div class="col">
                            <label>Kỳ hạn (tháng)</label>
                            <input type="number" name="term" class="form-control"
                                   value="${account.accountType == 'SavingsAccount' ? account.term : ''}">
                        </div>
                    </div>
                </div>

                <div id="paymentFields" class="p-3 border rounded bg-white mb-3" style="display: none;">
                    <h6 class="text-primary border-bottom pb-2">Thông tin Thanh Toán</h6>
                    <div class="mb-3">
                        <label>Số thẻ (Card Number)</label>
                        <input type="text" name="cardNumber" class="form-control"
                               value="${account.accountType == 'PaymentAccount' ? account.cardNumber : ''}">
                    </div>
                    <div class="mb-3">
                        <label>Số dư ban đầu</label>
                        <input type="number" name="balance" class="form-control"
                               value="<fmt:formatNumber value="${account.accountType == 'PaymentAccount' ? account.balance : 0}" pattern="#0"/>">
                    </div>
                </div>

                <div class="d-flex justify-content-end mt-4">
                    <a href="account" class="btn btn-secondary me-2">Quay lại</a>
                    <button type="submit" class="btn ${account != null ? 'btn-warning' : 'btn-success'}">
                        ${account != null ? 'Lưu Cập Nhật' : 'Tạo Mới'}
                    </button>
                </div>
            </form>
        </div>
    </div>
</div>

<script>
    function toggleFields() {
        let type = document.getElementById("typeSelect").value;
        let savingsDiv = document.getElementById("savingsFields");
        let paymentDiv = document.getElementById("paymentFields");

        if (type === "SAVINGS") {
            savingsDiv.style.display = "block";
            paymentDiv.style.display = "none";
        } else {
            savingsDiv.style.display = "none";
            paymentDiv.style.display = "block";
        }
    }
    window.onload = function() {
        toggleFields();
    };
</script>
<script>
    function setMaxDate() {
        let today = new Date();
        let dd = String(today.getDate()).padStart(2, '0');
        let mm = String(today.getMonth() + 1).padStart(2, '0');
        let yyyy = today.getFullYear();
        let currentDate = yyyy + '-' + mm + '-' + dd;
        let dateInput = document.getElementById("depositDate");
        if (dateInput) {
            dateInput.setAttribute("max", currentDate);
        }
    }
    window.onload = function() {
        toggleFields();
        setMaxDate();
    };
</script>
</body>
</html>