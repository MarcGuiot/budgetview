package org.designup.picsou.gui.accounts;

import org.designup.picsou.gui.accounts.utils.AccountTypeSelector;
import org.designup.picsou.model.Account;
import org.designup.picsou.model.AccountType;
import org.designup.picsou.model.AccountCardType;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.Utils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class AccountTypeCombo {
  protected JComboBox accountTypeCombo;
  private AccountTypeSelector[] accountTypeSelectors;
  private Glob currentAccount;

  public AccountTypeCombo(GlobRepository repository) {
    accountTypeSelectors = createTypeSelectors(repository);
  }

  public JComboBox createAccountTypeCombo() {
    accountTypeCombo = new JComboBox(accountTypeSelectors);

    accountTypeCombo.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        AccountTypeSelector selector = (AccountTypeSelector)accountTypeCombo.getSelectedItem();
        if (selector != null) {
          selector.apply();
        }
      }
    });
    return accountTypeCombo;
  }

  public void updateAccountTypeCombo(Glob account) {
    currentAccount = account;
    if (account == null) {
      accountTypeCombo.setSelectedIndex(-1);
    }
    else {
      for (AccountTypeSelector selector : accountTypeSelectors) {
        if (selector.isApplied(account)) {
          accountTypeCombo.setSelectedItem(selector);
        }
      }
    }
  }
  protected AccountTypeSelector[] createTypeSelectors(final GlobRepository repository) {
    return new AccountTypeSelector[]{
      new AccountTypeSelector("account.type.main") {
        public void apply() {
          repository.update(currentAccount.getKey(), Account.ACCOUNT_TYPE, AccountType.MAIN.getId());
          repository.update(currentAccount.getKey(), Account.CARD_TYPE, AccountCardType.NOT_A_CARD.getId());
        }

        public boolean isApplied(Glob account) {
          return Account.isMain(account) &&
                 account.get(Account.CARD_TYPE).equals(AccountCardType.NOT_A_CARD.getId());
        }
      },

      new AccountTypeSelector("accountCardType.credit") {
        public void apply() {
          repository.update(currentAccount.getKey(), Account.ACCOUNT_TYPE, AccountType.MAIN.getId());
          repository.update(currentAccount.getKey(), Account.CARD_TYPE, AccountCardType.CREDIT.getId());
        }

        public boolean isApplied(Glob account) {
          return Account.isMain(account) &&
                 Utils.equal(account.get(Account.CARD_TYPE), AccountCardType.CREDIT.getId());
        }
      },
      new AccountTypeSelector("accountCardType.deferred") {
        public void apply() {
          repository.update(currentAccount.getKey(), Account.ACCOUNT_TYPE, AccountType.MAIN.getId());
          repository.update(currentAccount.getKey(), Account.CARD_TYPE, AccountCardType.DEFERRED.getId());
        }

        public boolean isApplied(Glob account) {
          return Account.isMain(account) &&
                 Utils.equal(account.get(Account.CARD_TYPE), AccountCardType.DEFERRED.getId());
        }
      }
      ,

      new AccountTypeSelector("account.type.savings") {
        public void apply() {
          repository.update(currentAccount.getKey(), Account.ACCOUNT_TYPE, AccountType.SAVINGS.getId());
          repository.update(currentAccount.getKey(), Account.CARD_TYPE, AccountCardType.NOT_A_CARD.getId());
        }

        public boolean isApplied(Glob account) {
          return Account.isSavings(account);
        }
      },
    };
  }

  public void setEnabled(boolean editable) {
    accountTypeCombo.setEnabled(editable);
  }
}
