package com.example.its.ui.swing.model;

import javax.swing.DefaultComboBoxModel;
import java.util.List;

/**
 * Assignee 지정 드롭다운용 모델.
 * TODO: BE 완성 후 AccountResponse 리스트로 교체
 */
public class AccountComboBoxModel extends DefaultComboBoxModel<String> {

    // accountId와 displayName 매핑 보관
    private final java.util.List<Long> accountIds = new java.util.ArrayList<>();

    public void setAccounts(List<Long> ids, List<String> displayNames) {
        removeAllElements();
        accountIds.clear();
        for (int i = 0; i < ids.size(); i++) {
            accountIds.add(ids.get(i));
            addElement(displayNames.get(i));
        }
    }

    public Long getSelectedAccountId() {
        int idx = getIndexOf(getSelectedItem());
        if (idx < 0 || idx >= accountIds.size()) return null;
        return accountIds.get(idx);
    }
}
