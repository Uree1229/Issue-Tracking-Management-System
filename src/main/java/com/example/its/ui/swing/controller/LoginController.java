package com.example.its.ui.swing.controller;

import com.example.its.ui.swing.view.LoginView;
import com.example.its.ui.swing.view.MainFrame;

public class LoginController {

    private final LoginView view;
    private final MainFrame mainFrame;
    // TODO: private final AccountFacade accountFacade;

    public LoginController(LoginView view, MainFrame mainFrame) {
        this.view = view;
        this.mainFrame = mainFrame;
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

        // TODO: BE 연결 후 아래 주석 해제
        // try {
        //     AccountResponse account = accountFacade.login(loginId, password);
        //     SessionContext.setCurrentAccount(account);
        //     view.clearFields();
        //     view.clearError();
        //     mainFrame.showProjectList();
        // } catch (Exception ex) {
        //     view.showError("아이디 또는 비밀번호가 올바르지 않습니다.");
        // }

        // 임시: 화면 전환 확인용
        view.clearError();
        view.clearFields();
        mainFrame.showProjectList();
    }
}
