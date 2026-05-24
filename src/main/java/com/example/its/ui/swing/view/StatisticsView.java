package com.example.its.ui.swing.view;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;

public class StatisticsView extends JPanel {

    // TODO: BE 연결 후 실제 차트 라이브러리(JFreeChart 등)로 교체 가능
    // 현재는 텍스트 기반으로 통계 표시
    private final JTextArea dailyArea    = new JTextArea();
    private final JTextArea monthlyArea  = new JTextArea();
    private final JTextArea statusArea   = new JTextArea();

    private final JButton backButton     = new JButton("← 이슈 목록");
    private final JButton refreshButton  = new JButton("새로고침");

    public StatisticsView() {
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        initComponents();
    }

    private void initComponents() {
        JPanel chartsPanel = new JPanel(new GridLayout(1, 3, 8, 0));

        dailyArea.setEditable(false);
        monthlyArea.setEditable(false);
        statusArea.setEditable(false);

        JPanel dailyPanel = new JPanel(new BorderLayout());
        dailyPanel.setBorder(BorderFactory.createTitledBorder("일별 이슈 발생"));
        dailyPanel.add(new JScrollPane(dailyArea));
        chartsPanel.add(dailyPanel);

        JPanel monthlyPanel = new JPanel(new BorderLayout());
        monthlyPanel.setBorder(BorderFactory.createTitledBorder("월별 이슈 발생"));
        monthlyPanel.add(new JScrollPane(monthlyArea));
        chartsPanel.add(monthlyPanel);

        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.setBorder(BorderFactory.createTitledBorder("상태별 집계"));
        statusPanel.add(new JScrollPane(statusArea));
        chartsPanel.add(statusPanel);

        add(new JLabel("이슈 통계", JLabel.CENTER), BorderLayout.NORTH);
        add(chartsPanel, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.add(backButton);
        btnPanel.add(refreshButton);
        add(btnPanel, BorderLayout.SOUTH);
    }

    // TODO: StatisticsResponse 연결 후 실제 데이터 바인딩
    public void setDailyStats(String text)   { dailyArea.setText(text); }
    public void setMonthlyStats(String text) { monthlyArea.setText(text); }
    public void setStatusStats(String text)  { statusArea.setText(text); }

    public JButton getBackButton()    { return backButton; }
    public JButton getRefreshButton() { return refreshButton; }
}
