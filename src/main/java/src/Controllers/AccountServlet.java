package src.Controllers;

import src.DAO.AccountDAO;
import src.Entities.*;
import src.Validate.Validator;
import src.Utils.DateUtils;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;
import java.io.IOException;
import java.util.List;

@WebServlet("/account")
public class AccountServlet extends HttpServlet {
    private AccountDAO accountDAO = new AccountDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (request.getSession().getAttribute("user") == null) {
            response.sendRedirect("login");
            return;
        }

        String action = request.getParameter("action");
        if (action == null) action = "list";

        switch (action) {
            case "new":
                showNewForm(request, response);
                break;
            case "delete":
                deleteAccount(request, response);
                break;
            case "search":
                searchAccount(request, response);
                break;
            case "edit":
                showEditForm(request, response);
            default:
                listAccounts(request, response);
                break;
        }
    }

    private void listAccounts(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        int accPage = 1;
        int accSize = 10;

        if (request.getParameter("page") != null) {
            try {
                accPage = Integer.parseInt(request.getParameter("page"));
                if (accPage < 1) accPage = 1;
            } catch (NumberFormatException e) {
                accPage = 1;
            }
        }
        int totalAccounts = accountDAO.countTotalAccounts();
        int totalAccPages = (int) Math.ceil((double) totalAccounts / accSize);
        if (totalAccPages == 0) totalAccPages = 1;

        List<BankAccount> list = accountDAO.getAccountsByPage(accPage, accSize);

        request.setAttribute("listAccounts", list);
        request.setAttribute("currentAccPage", accPage);
        request.setAttribute("totalAccPages", totalAccPages);

        HttpSession session = request.getSession();
        String role = (String) session.getAttribute("role");

        int logPage = 1;
        int logSize = 10;

        int totalLogPages = 0;

        if ("ADMIN".equalsIgnoreCase(role)) {
            if (request.getParameter("logPage") != null) {
                try {
                    logPage = Integer.parseInt(request.getParameter("logPage"));
                    if (logPage < 1) logPage = 1;
                } catch (NumberFormatException e) {
                    logPage = 1;
                }
            }

            int totalLogs = accountDAO.countTotalLogs();
            totalLogPages = (int) Math.ceil((double) totalLogs / logSize);
            if (totalLogPages == 0) totalLogPages = 1;

            List<ActivityLog> logs = accountDAO.getLogsByPage(logPage, logSize);
            request.setAttribute("systemLogs", logs);
        }

        request.setAttribute("currentLogPage", logPage);
        request.setAttribute("totalLogPages", totalLogPages);

        request.getRequestDispatcher("/WEB-INF/views/dashboard.jsp").forward(request, response);
    }

    private void searchAccount(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String keyword = request.getParameter("keyword");
        List<BankAccount> list = accountDAO.searchAccounts(keyword);
        request.setAttribute("listAccounts", list);
        request.setAttribute("currentAccPage", 1);
        request.setAttribute("totalAccPages", 1);
        request.getRequestDispatcher("/WEB-INF/views/dashboard.jsp").forward(request, response);
    }

    private void showNewForm(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.getRequestDispatcher("/WEB-INF/views/add-account.jsp").forward(request, response);
    }

    private void showEditForm(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String code = request.getParameter("code");
        BankAccount existingAccount = accountDAO.getAccountByCode(code);
        if (existingAccount == null) {
            response.sendRedirect("account");
            return;
        }
        request.setAttribute("account", existingAccount);
        request.getRequestDispatcher("WEB-INF/views/add-account.jsp").forward(request, response);
    }

    private void deleteAccount(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String code = request.getParameter("code");
        String currentUser = (String) request.getSession().getAttribute("user");
        try {
            accountDAO.deleteAccount(code, currentUser);
        } catch (Exception e) {
            e.printStackTrace();
        }
        response.sendRedirect("account");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        String action = req.getParameter("action");
        if (action == null) action = "list";

        switch (action) {
            case "insert": insertAccount(req, resp); break;
            case "update": updateAccount(req, resp); break;
            default: resp.sendRedirect("account"); break;
        }
    }

    private void insertAccount(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        try {
            String code = request.getParameter("code");
            String name = request.getParameter("name");
            String cid = request.getParameter("citizenId");
            String type = request.getParameter("type");

            if (!Validator.isValidAccountCode(code)) {
                throw new IllegalArgumentException("Mã tài khoản không hợp lệ phải đủ 9 số!");
            }
            if (Validator.isEmpty(name) || Validator.isEmpty(cid)) {
                throw new IllegalArgumentException("Tên và CCCD không được để trống!");
            }
            if (!Validator.isValidCitizenId(cid)) {
                throw new IllegalArgumentException("Số CCCD phải là 9 hoặc 12 chữ số!");
            }
            if(accountDAO.isCodeExist(code)) {
                throw new IllegalArgumentException("Mã tài khoản đã tồn tại!");
            }

            BankAccount acc = null;
            if ("SAVINGS".equals(type)) {
                String depositStr = request.getParameter("depositAmount");
                String dateStr = request.getParameter("depositDate");
                String rateStr = request.getParameter("interestRate");
                String termStr = request.getParameter("term");

                if (!Validator.isPositiveNumber(depositStr)) {
                    throw new IllegalArgumentException("Số tiền gửi phải lớn hơn 0!");
                }
                if (!Validator.isPositiveNumber(rateStr)) {
                    throw new IllegalArgumentException("Lãi suất phải lớn hơn 0!");
                }
                if (!Validator.isPositiveNumber(termStr)) {
                    throw new IllegalArgumentException("Kỳ hạn phải lớn hơn 0!");
                }
                if (DateUtils.isFutureDate(dateStr)) {
                    throw new IllegalArgumentException("Ngày gửi tiền không được lớn hơn ngày hiện tại!");
                }

                double amount = Double.parseDouble(depositStr.replace(",", ""));
                double rate = Double.parseDouble(rateStr.replace(",", ""));
                int term = Integer.parseInt(termStr);
                String finalDate = DateUtils.convertToSqlDate(dateStr).toString();

                acc = new SavingsAccount(0, code, 0, name, cid, finalDate, amount, finalDate, rate, term);
            } else {
                String cardNum = request.getParameter("cardNumber");
                String balanceStr = request.getParameter("balance");

                if (!Validator.isPositiveNumber(balanceStr)) {
                    throw new IllegalArgumentException("Số dư ban đầu phải lớn hơn 0!");
                }
                double balance = Double.parseDouble(balanceStr.replace(",", ""));
                String today = java.time.LocalDate.now().toString();

                acc = new PaymentAccount(0, code, 0, name, cid, today, cardNum, balance);
            }

            String currentUser = (String) request.getSession().getAttribute("user");
            if (accountDAO.addAccount(acc, currentUser)) {
                response.sendRedirect("account");
            } else {
                request.setAttribute("error", "Lỗi Database!");
                showNewForm(request, response);
            }
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "Lỗi: " + e.getMessage());
            showNewForm(request, response);
        }
    }

    private void updateAccount(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            String code = req.getParameter("code");
            String name = req.getParameter("name");
            String citizenId = req.getParameter("citizenId");
            String type = req.getParameter("type");

            if (!Validator.isValidCitizenId(citizenId)) {
                throw new IllegalArgumentException("Số CCCD không hợp lệ (9 hoặc 12 số)!");
            }

            BankAccount account = null;
            if ("SAVINGS".equals(type)) {
                String depositStr = req.getParameter("depositAmount");
                String dateStr = req.getParameter("depositDate");
                String rateStr = req.getParameter("interestRate");
                String termStr = req.getParameter("term");

                if (!Validator.isPositiveNumber(depositStr)) {
                    throw new IllegalArgumentException("Số tiền gửi không hợp lệ!");
                }
                if (!Validator.isPositiveNumber(rateStr)) {
                    throw new IllegalArgumentException("Lãi suất phải lớn hơn 0!");
                }
                if (!Validator.isPositiveNumber(termStr)) {
                    throw new IllegalArgumentException("Kỳ hạn phải lớn hơn 0!");
                }
                if (DateUtils.isFutureDate(dateStr)) {
                    throw new IllegalArgumentException("Ngày gửi không được ở tương lai!");
                }

                double deposit = Double.parseDouble(depositStr.replace(",", ""));
                double rate = Double.parseDouble(rateStr.replace(",", ""));
                int term = Integer.parseInt(termStr);
                String finalDate = DateUtils.convertToSqlDate(dateStr).toString();

                account = new SavingsAccount(0, code, 0, name, citizenId, finalDate, deposit, finalDate, rate, term);
            } else if ("PAYMENT".equals(type)) {
                String card = req.getParameter("cardNumber");
                String balanceStr = req.getParameter("balance");

                if (!Validator.isPositiveNumber(balanceStr)) {
                    throw new IllegalArgumentException("Số dư không hợp lệ!");
                }
                double balance = Double.parseDouble(balanceStr.replace(",", ""));

                account = new PaymentAccount(0, code, 0, name, citizenId, "", card, balance);
            }

            if (account != null) {
                HttpSession session = req.getSession();
                String currentAdmin = (String) session.getAttribute("user");
                if (currentAdmin == null) currentAdmin = "Unknown";

                boolean success = accountDAO.updateAccount(account, currentAdmin);
                if (!success) System.out.println("Update thất bại DAO");
            }
            resp.sendRedirect("account");

        } catch (Exception e) {
            e.printStackTrace();
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Lỗi cập nhật: " + e.getMessage());
        }
    }
}