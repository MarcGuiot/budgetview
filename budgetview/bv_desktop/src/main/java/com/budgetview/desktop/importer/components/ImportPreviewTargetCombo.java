package com.budgetview.desktop.importer.components;

import com.budgetview.desktop.accounts.utils.AccountMatchers;
import com.budgetview.desktop.description.stringifiers.AccountComparator;
import com.budgetview.desktop.importer.utils.AccountFinder;
import com.budgetview.model.Account;
import com.budgetview.model.RealAccount;
import com.budgetview.utils.Lang;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.model.utils.GlobMatchers;
import org.globsframework.utils.Strings;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ImportPreviewTargetCombo {

  public interface Callback {
    void processNewAccount();

    void processAccount(Glob account);

    void skipAccount();
  }

  private GlobRepository repository;
  private GlobRepository globalRepository;
  private Callback callback;
  private JComboBox combo;
  private int newAccountIndex;
  private Map<Integer, Integer> accountIndexes = new HashMap<Integer, Integer>();
  private int skipIndex;


  public ImportPreviewTargetCombo(GlobRepository repository, GlobRepository globalRepository, Callback callback) {
    this.repository = repository;
    this.globalRepository = globalRepository;
    this.callback = callback;
    createCombo();
  }

  private void createCombo() {
    combo = new JComboBox();
    combo.addActionListener(new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
        Object item = combo.getSelectedItem();
        if (item != null) {
          Action action = (Action) item;
          action.actionPerformed(null);
        }
      }
    });
  }

  public void preselectBestAccount(Integer accountId, Glob realAccount, GlobList importedTransactions) {
    setNewModel();
    if (accountId != null && repository.contains(Key.create(Account.TYPE, accountId))) {
      selectAccount(accountId);
    }
    else if (importedTransactions.isEmpty()) {
      selectSkipAccount();
    }
    else {
      accountId = AccountFinder.findBestAccount(importedTransactions, globalRepository);

      Glob associatedImportedAccount = repository.getAll(RealAccount.TYPE)
        .filter(GlobMatchers.fieldEquals(RealAccount.ACCOUNT, accountId), repository)
        .getFirst();
      if (associatedImportedAccount != null && !RealAccount.areNearEquivalent(associatedImportedAccount, realAccount)) {
        accountId = null;
      }

      if (accountId != null && !Account.SUMMARY_ACCOUNT_IDS.contains(accountId)) {
        selectAccount(accountId);
      }
      else {
        selectNewAccount();
      }
    }
    combo.repaint();   // necessaire sous linux au moins
  }

  private void setNewModel() {
    final List<Action> actions = new ArrayList<Action>();
    accountIndexes.clear();

    actions.add(new ComboAction(Lang.get("import.preview.targetCombo.newAccount")) {
      public void actionPerformed(ActionEvent e) {
        callback.processNewAccount();
      }
    });
    newAccountIndex = 0;

    int index = 1;
    for (final Glob account : repository.getAll(Account.TYPE, AccountMatchers.nonSummaryAccounts()).sortSelf(new AccountComparator())) {
      actions.add(new ComboAction(Lang.get("import.preview.targetCombo.existingAccount", account.get(Account.NAME))) {
        public void actionPerformed(ActionEvent e) {
          callback.processAccount(account);
        }
      });
      accountIndexes.put(account.get(Account.ID), index++);
    }

    actions.add(new ComboAction(Lang.get("import.preview.targetCombo.skip")) {
      public void actionPerformed(ActionEvent e) {
        callback.skipAccount();
      }
    });
    skipIndex = index;

    combo.setModel(new DefaultComboBoxModel(actions.toArray(new Object[actions.size()])));
  }

  private void selectNewAccount() {
    combo.setSelectedIndex(newAccountIndex);
  }

  private void selectAccount(int accountId) {
    Integer index = accountIndexes.get(accountId);
    if (index != null) {
      combo.setSelectedIndex(index);
    }
    else {
      selectNewAccount();
    }
  }

  private void selectSkipAccount() {
    combo.setSelectedIndex(skipIndex);
  }

  public JComboBox get() {
    return combo;
  }

  private abstract class ComboAction extends AbstractAction {
    public ComboAction(String name) {
      super(name);
    }

    public String toString() { // For tests
      return Strings.toString(getValue(Action.NAME));
    }
  }
}
