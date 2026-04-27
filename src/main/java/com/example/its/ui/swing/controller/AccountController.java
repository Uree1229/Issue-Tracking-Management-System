package com.example.its.ui.swing.controller;

import com.example.its.ui.swing.view.AccountManageView;
import com.example.its.ui.swing.view.MainFrame;
import com.example.its.ui.swing.view.dialog.AccountCreateDialog;

public class AccountController {

    private final AccountManageView view;
    private final MainFrame mainFrame;
    // TODO: private final AccountFacade accountFacade;

    public AccountController(AccountManageView view, MainFrame mainFrame) {
        this.view = view;
        this.mainFrame = mainFrame;
        initListeners();
    }

    private void initListeners() {
        view.getCreateButton().addActionListener(e -> handleCreateAccount());
        view.getDeactivateButton().addActionListener(e -> handleDeactivate());
        view.getBackButton().addActionListener(e -> mainFrame.showProjectList());
    }

    public void loadAccounts() {
        // TODO: List<AccountResponse> accounts = accountFacade.getAllAccounts();
        // Object[][] data = accounts.stream()
        //     .map(a -> new Object[]{a.getAccountId(), a.getLoginId(), a.getName(),
        //                            a.getEmail(), a.getRole(), a.isActive()})
        //     .toArray(Object[][]::new);
        // view.setAccounts(data);

        // 임시 더미 데이터
        view.setAccounts(new Object[][]{
            {1L, "admin", "관리자", "admin@its.com", "ADMIN", true},
            {2L, "dev1",  "개발자1", "dev1@its.com",  "DEV",   true}
        });
    }

    private void handleCreateAccount() {
        AccountCreateDialog dialog = new AccountCreateDialog(mainFrame);
        dialog.getCreateButton().addActionListener(e -> {
            String loginId  = dialog.getLoginId();
            String password = dialog.getPassword();
            String name     = dialog.getName();
            String email    = dialog.getEmail();
            String role     = dialog.getRole();

            if (loginId.isEmpty() || password.isEmpty() || name.isEmpty() || email.isEmpty()) {
                dialog.showError("모든 필수 항목을 입력하세요.");
                return;
            }

            // TODO: accountFacade.createAccount(new AccountCreateRequest(loginId, password, name, email, role));
            loadAccounts();
            dialog.dispose();
        });
        dialog.setVisible(true);
    }

    private void handleDeactivate() {
        int row = view.getSelectedRow();
        if (row < 0) return;
        // TODO: Long accountId = (Long) view.getValueAt(row, 0);
        // accountFacade.deactivateAccount(accountId);
        // loadAccounts();
    }
}
