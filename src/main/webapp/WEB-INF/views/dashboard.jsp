<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<!DOCTYPE html>
<html lang="vi">
<head>
    <title>Quản lý Ngân hàng</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.0/font/bootstrap-icons.css">
    <link href="${pageContext.request.contextPath}/css/style.css" rel="stylesheet">
</head>
<body> <nav class="navbar navbar-expand-lg navbar-dark navbar-custom px-4 shadow-sm mb-4 sticky-top">
    <a class="navbar-brand fw-bold d-flex align-items-center" href="account">
        <i class="bi bi-bank2 me-2 fs-4"></i> E-BANKING SYSTEM
    </a>
    <div class="collapse navbar-collapse justify-content-end">
        <div class="d-flex align-items-center">
            <div class="text-white me-3 text-end">
                <small class="d-block text-white-50" style="font-size: 0.75rem;">Xin chào,</small>
                <span class="fw-bold">${sessionScope.user}</span>
            </div>
            <a href="logout" class="btn btn-outline-light btn-sm rounded-pill px-3">
                <i class="bi bi-box-arrow-right"></i> Thoát
            </a>
        </div>
    </div>
</nav>

<div class="container-fluid px-5 pb-5">

    <div class="d-flex justify-content-between align-items-center mb-4">
        <div>
            <h3 class="fw-bold text-dark mb-1">Danh Sách Tài Khoản</h3>
            <p class="text-muted mb-0">Quản lý thông tin khách hàng và số dư.</p>
        </div>
        <div class="d-flex gap-2">
            <button onclick="exportTableToExcel('accountTable', 'Danh_Sach_Tai_Khoan')" class="btn btn-success shadow-sm">
                <i class="bi bi-file-earmark-excel"></i> Xuất Excel
            </button>
            <a href="account?action=new" class="btn btn-primary shadow-sm fw-bold px-4">
                <i class="bi bi-plus-lg"></i> Thêm Mới
            </a>
        </div>
    </div>

    <div class="card table-card">
        <div class="card-body p-0">
            <div class="p-4 bg-white border-bottom">
                <div class="row">
                    <div class="col-md-5">
                        <form action="${pageContext.request.contextPath}/account" method="get" class="input-group search-box">
                            <input type="hidden" name="action" value="search">
                            <input type="text" name="keyword" class="form-control border"
                                   placeholder="Tìm theo Mã TK, Tên hoặc CCCD..." value="${param.keyword}">
                            <button type="submit" class="btn btn-primary"><i class="bi bi-search"></i></button>
                        </form>
                    </div>
                </div>
            </div>

            <div class="table-responsive">
                <table class="table table-hover mb-0" id="accountTable">
                    <thead>
                    <tr>
                        <th class="text-center" width="5%">#</th>
                        <th width="15%">Mã Tài Khoản</th>
                        <th width="25%">Chủ Tài Khoản</th>
                        <th width="15%" class="text-center">Loại</th>
                        <th width="20%" class="text-end">Số dư / Tiền gửi</th>
                        <th width="20%">Chi tiết</th>
                        <th class="no-print text-center" width="100px">Thao tác</th>
                    </tr>
                    </thead>
                    <tbody>
                    <c:if test="${empty listAccounts}">
                        <tr><td colspan="7" class="text-center py-5 text-muted">
                            <i class="bi bi-inbox fs-1 d-block mb-2"></i>
                            Không tìm thấy dữ liệu nào.
                        </td></tr>
                    </c:if>

                    <c:forEach var="acc" items="${listAccounts}" varStatus="status">
                        <tr>
                            <td class="text-center text-muted">${status.index + 1}</td>
                            <td class="fw-bold text-primary font-monospace">${acc.accountCode}</td>
                            <td>
                                <div class="d-flex align-items-center">
                                    <div class="avatar-circle">
                                            ${acc.ownerName.substring(0,1)}
                                    </div>
                                    <div>
                                        <div class="fw-bold text-dark">${acc.ownerName}</div>
                                        <small class="text-muted"><i class="bi bi-card-heading"></i> ${acc.citizenId}</small>
                                    </div>
                                </div>
                            </td>

                            <c:choose>
                                <c:when test="${acc.accountType == 'SavingsAccount'}">
                                    <td class="text-center"><span class="badge rounded-pill bg-success bg-opacity-10 text-success border border-success border-opacity-25 px-3">Tiết Kiệm</span></td>
                                    <td class="text-end fw-bold text-success fs-6">
                                        <fmt:formatNumber value="${acc.depositAmount}" pattern="#,###"/> đ
                                    </td>
                                    <td>
                                        <div style="font-size: 0.85rem;">
                                            <div class="text-muted mb-1"><i class="bi bi-calendar-check me-1"></i> Gửi: ${acc.depositDate}</div>
                                            <span class="badge bg-secondary me-1">${acc.interestRate}%/năm</span>
                                            <span class="badge bg-light text-dark border">${acc.term} tháng</span>
                                        </div>
                                    </td>
                                </c:when>
                                <c:otherwise>
                                    <td class="text-center"><span class="badge rounded-pill bg-primary bg-opacity-10 text-primary border border-primary border-opacity-25 px-3">Thanh Toán</span></td>
                                    <td class="text-end fw-bold text-primary fs-6">
                                        <fmt:formatNumber value="${acc.balance}" pattern="#,###"/> đ
                                    </td>
                                    <td>
                                        <small class="text-muted"><i class="bi bi-credit-card me-1"></i> Số thẻ:</small><br>
                                        <span class="font-monospace">${acc.cardNumber}</span>
                                    </td>
                                </c:otherwise>
                            </c:choose>

                            <td class="text-center no-print">
                                <div class="btn-group" role="group">
                                    <a href="account?action=edit&code=${acc.accountCode}" class="btn btn-light btn-sm text-primary" data-bs-toggle="tooltip" title="Sửa">
                                        <i class="bi bi-pencil-square fs-5"></i>
                                    </a>
                                    <a href="account?action=delete&code=${acc.accountCode}"
                                       class="btn btn-light btn-sm text-danger"
                                       title="Xóa"
                                       onclick="return confirm('Bạn có chắc chắn muốn XÓA tài khoản ${acc.accountCode}? Hành động này không thể hoàn tác!');">
                                        <i class="bi bi-trash fs-5"></i>
                                    </a>
                                </div>
                            </td>
                        </tr>
                    </c:forEach>
                    <c:if test="${sessionScope.role == 'ADMIN'}">
                        <div class="card table-card mt-5 mb-5 border-danger">
                            <div class="card-header bg-danger text-white fw-bold d-flex justify-content-between align-items-center">
                                <span><i class="bi bi-shield-lock-fill"></i> NHẬT KÝ HOẠT ĐỘNG</span>
                                <span class="badge bg-white text-danger">Trang ${currentLogPage}/${totalLogPages}</span>
                            </div>
                            <div class="card-body p-0">
                                <table class="table table-sm table-striped mb-0">
                                    <thead class="table-light">
                                    <tr>
                                        <th>Thời gian</th>
                                        <th>Người dùng</th>
                                        <th>Hành động</th>
                                    </tr>
                                    </thead>
                                    <tbody>
                                    <c:forEach var="log" items="${systemLogs}">
                                        <tr>
                                            <td>${log.logTime}</td>
                                            <td class="fw-bold text-danger">${log.username}</td>
                                            <td>${log.action}</td>
                                        </tr>
                                    </c:forEach>
                                    </tbody>
                                </table>

                                <c:if test="${totalLogPages > 1}">
                                    <div class="p-2 bg-light border-top">
                                        <ul class="pagination pagination-sm justify-content-end mb-0">
                                            <li class="page-item ${currentLogPage == 1 ? 'disabled' : ''}">
                                                <a class="page-link" href="account?page=${currentAccPage}&logPage=${currentLogPage - 1}">Log Trước</a>
                                            </li>

                                            <c:forEach begin="1" end="${totalLogPages}" var="j">
                                                <c:if test="${j == 1 || j == totalLogPages || (j >= currentLogPage - 1 && j <= currentLogPage + 1)}">
                                                    <li class="page-item ${currentLogPage == j ? 'active' : ''}">
                                                        <a class="page-link" href="account?page=${currentAccPage}&logPage=${j}">${j}</a>
                                                    </li>
                                                </c:if>
                                            </c:forEach>

                                            <li class="page-item ${currentLogPage == totalLogPages ? 'disabled' : ''}">
                                                <a class="page-link" href="account?page=${currentAccPage}&logPage=${currentLogPage + 1}">Log Sau</a>
                                            </li>
                                        </ul>
                                    </div>
                                </c:if>
                            </div>
                        </div>
                    </c:if>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
</div>

<script>
    function exportTableToExcel(tableID, filename = ''){
        let downloadLink;
        let dataType = 'application/vnd.ms-excel';
        let tableSelect = document.getElementById(tableID);
        let tableClone = tableSelect.cloneNode(true);
        let elementsToRemove = tableClone.querySelectorAll(".no-print");
        elementsToRemove.forEach(el => el.remove());
        tableClone.style.border = '1px solid black';
        let tableHTML = tableClone.outerHTML.replace(/ /g, '%20');
        let htmlContent = '\uFEFF' + tableClone.outerHTML;
        filename = filename ? filename + '.xls' : 'excel_data.xls';
        downloadLink = document.createElement("a");
        document.body.appendChild(downloadLink);
        if(navigator.msSaveOrOpenBlob){
            let blob = new Blob(['\ufeff', tableHTML], { type: dataType });
            navigator.msSaveOrOpenBlob( blob, filename);
        } else {
            downloadLink.href = 'data:' + dataType + ', ' + htmlContent;
            downloadLink.download = filename;
            downloadLink.click();
        }
    }
</script>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>

</body>
</html>