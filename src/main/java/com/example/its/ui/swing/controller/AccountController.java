package com.example.its.ui.swing.controller;

import com.example.its.application.facade.AccountFacade;
import com.example.its.persistence.entity.Role;
import com.example.its.shared.dto.account.AccountCreateRequest;
import com.example.its.shared.dto.account.AccountResponse;
import com.example.its.ui.swing.view.AccountManageView;
import com.example.its.ui.swing.view.MainFrame;
import com.example.its.ui.swing.view.dialog.AccountCreateDialog;

import java.util.List;

public class AccountController {

    private final AccountManageView view;
    private final MainFrame mainFrame;
    private final AccountFacade accountFacade;

    public AccountController(AccountManageView view, MainFrame mainFrame, AccountFacade accountFacade) {
        this.view = view;
        this.mainFrame = mainFrame;
        this.accountFacade = accountFacade;
        initListeners();
    }

    private void initListeners() {
        view.getCreateButton().addActionListener(e -> handleCreateAccount());
        view.getDeactivateButton().addActionListener(e -> handleDeactivate());
        view.getBackButton().addActionListener(e -> mainFrame.showProjectList());
    }

    public void loadAccounts() {
        try {
            List<AccountResponse> accounts = accountFacade.getActiveAccounts();
            Object[][] data = accounts.stream()
                .map(a -> new Object[]{
                    a.getAccountId(), a.getLoginId(), a.getName(),
                    a.getEmail(), a.getRole().name(), a.isActive()
                })
                .toArray(Object[][]::new);
            view.setAccounts(data);
        } catch (Exception ex) {
            view.setAccounts(new Object[0][]);
        }
    }

    private void handleCreateAccount() {
        AccountCreateDialog dialog = new AccountCreateDialog(mainFrame);
        dialog.getCreateButton().addActionListener(e -> {
            String loginId  = dialog.getLoginId();
            String password = dialog.getPassword();
            String name     = dialog.getName();
            String email    = dialog.getEmail();

            if (loginId.isEmpty() || password.isEmpty() || name.isEmpty() || email.isEmpty()) {
                dialog.showError("모든 필수 항목을 입력하세요.");
                return;
            }

            try {
                Role role = Role.valueOf(dialog.getRole());
                accountFacade.register(new AccountCreateRequest(loginId, password, name, email, role));
                loadAccounts();
                dialog.dispose();
            } catch (Exception ex) {
                dialog.showError("계정 생성 실패: " + ex.getMessage());
            }
        });
        dialog.setVisible(true);
    }

    private void handleDeactivate() {
        // TODO: AccountFacade에 deactivateAccount 메서드 노출 필요
    }
}
