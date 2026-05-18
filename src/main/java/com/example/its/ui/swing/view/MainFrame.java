package com.example.its.ui.swing.view;

import com.example.its.application.facade.AccountFacade;
import com.example.its.application.facade.IssueFacade;
import com.example.its.application.facade.ProjectFacade;
import com.example.its.shared.dto.account.AccountResponse;
import com.example.its.ui.swing.SessionContext;
import com.example.its.ui.swing.controller.AccountController;
import com.example.its.ui.swing.controller.IssueController;
import com.example.its.ui.swing.controller.LoginController;
import com.example.its.ui.swing.controller.ProjectController;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.FlowLayout;

public class MainFrame extends JFrame {

    public static final String LOGIN          = "LOGIN";
    public static final String PROJECT_LIST   = "PROJECT_LIST";
    public static final String ISSUE_LIST     = "ISSUE_LIST";
    public static final String ISSUE_DETAIL   = "ISSUE_DETAIL";
    public static final String ISSUE_CREATE   = "ISSUE_CREATE";
    public static final String STATISTICS     = "STATISTICS";
    public static final String ACCOUNT_MANAGE = "ACCOUNT_MANAGE";

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel cardPanel = new JPanel(cardLayout);
    private final JLabel statusBar = new JLabel("  로그인되지 않음");

    private final LoginView loginView             = new LoginView();
    private final ProjectListView projectListView = new ProjectListView();
    private final IssueListView issueListView     = new IssueListView();
    private final IssueDetailView issueDetailView = new IssueDetailView();
    private final IssueCreateView issueCreateView = new IssueCreateView();
    private final StatisticsView statisticsView   = new StatisticsView();
    private final AccountManageView accountManageView = new AccountManageView();

    private ProjectController projectController;
    private AccountController accountController;

    public MainFrame() {
        setTitle("ITS - Issue Tracking System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);

        cardPanel.add(loginView,         LOGIN);
        cardPanel.add(projectListView,   PROJECT_LIST);
        cardPanel.add(issueListView,     ISSUE_LIST);
        cardPanel.add(issueDetailView,   ISSUE_DETAIL);
        cardPanel.add(issueCreateView,   ISSUE_CREATE);
        cardPanel.add(statisticsView,    STATISTICS);
        cardPanel.add(accountManageView, ACCOUNT_MANAGE);

        add(cardPanel, BorderLayout.CENTER);

        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        statusPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.LIGHT_GRAY));
        statusPanel.add(statusBar);
        add(statusPanel, BorderLayout.SOUTH);
        initControllers();
    }

    private void initControllers() {
        AccountFacade accountFacade = new AccountFacade();
        ProjectFacade projectFacade = new ProjectFacade();
        IssueFacade issueFacade    = new IssueFacade();

        new LoginController(loginView, this, accountFacade);

        this.projectController = new ProjectController(projectListView, this, projectFacade);

        IssueController issueController =
            new IssueController(issueListView, issueDetailView, issueCreateView, this, issueFacade, accountFacade, projectFacade);

        projectController.setIssueController(issueController);

        this.accountController =
            new AccountController(accountManageView, this, accountFacade);
        accountController.loadAccounts();
    }

    public void showLogin() {
        statusBar.setText("  로그인되지 않음");
        cardLayout.show(cardPanel, LOGIN);
    }

    public void showIssueList() {
        issueListView.clearSelection();
        cardLayout.show(cardPanel, ISSUE_LIST);
    }

    public void showIssueDetail() { cardLayout.show(cardPanel, ISSUE_DETAIL); }
    public void showIssueCreate() { cardLayout.show(cardPanel, ISSUE_CREATE); }
    public void showStatistics()  { cardLayout.show(cardPanel, STATISTICS); }

    public void showAccountManage() {
        if (accountController != null) accountController.loadAccounts();
        cardLayout.show(cardPanel, ACCOUNT_MANAGE);
    }

    public void showProjectList() {
        updateStatusBar();
        if (projectController != null) projectController.loadProjects();
        cardLayout.show(cardPanel, PROJECT_LIST);
    }

    private void updateStatusBar() {
        AccountResponse acc = SessionContext.getCurrentAccount();
        if (acc != null) {
            statusBar.setText("  " + acc.getName() + "  [" + acc.getRole().name() + "]");
        } else {
            statusBar.setText("  로그인되지 않음");
        }
    }

    public LoginView getLoginView()               { return loginView; }
    public ProjectListView getProjectListView()   { return projectListView; }
    public IssueListView getIssueListView()       { return issueListView; }
    public IssueDetailView getIssueDetailView()   { return issueDetailView; }
    public IssueCreateView getIssueCreateView()   { return issueCreateView; }
    public StatisticsView getStatisticsView()     { return statisticsView; }
    public AccountManageView getAccountManageView() { return accountManageView; }
}
