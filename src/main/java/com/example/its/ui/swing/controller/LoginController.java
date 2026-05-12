package com.example.its.ui.swing.controller;

import com.example.its.application.facade.AccountFacade;
import com.example.its.shared.dto.account.AccountResponse;
import com.example.its.ui.swing.SessionContext;
import com.example.its.ui.swing.view.LoginView;
import com.example.its.ui.swing.view.MainFrame;

public class LoginController {

    private final LoginView view;
    private final MainFrame mainFrame;
    private final AccountFacade accountFacade;

    public LoginController(LoginView view, MainFrame mainFrame, AccountFacade accountFacade) {
        this.view = view;
        this.mainFrame = mainFrame;
        this.accountFacade = accountFacade;
        initListeners();
    }

    private void initListeners() {
        view.getLoginButton().addActionListener(e -> handleLogin());
    }

    private void handleLogin() {
        String loginId = view.getLoginId();
        String password = view.getPassword();

        if (loginId.isEmpty() || password.isEmpty()) {
            view.showError("아이디와 비밀번호를 입력하세요.");
            return;
        }

        try {
            AccountResponse account = accountFacade.login(loginId, password);
            SessionContext.setCurrentAccount(account);
            view.clearFields();
            view.clearError();
            mainFrame.showProjectList();
        } catch (Exception ex) {
            view.showError("아이디 또는 비밀번호가 올바르지 않습니다.");
        }
    }
}
