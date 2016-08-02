package com.budgetview.desktop.accounts;

import com.budgetview.desktop.accounts.utils.AccountTypeSelector;
import com.budgetview.model.Account;
import com.budgetview.model.AccountCardType;
import com.budgetview.model.AccountType;
import org.globsframework.metamodel.fields.LinkField;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.Utils;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public abstract class AccountTypeCombo {
  protected JComboBox accountTypeCombo;
  private AccountTypeSelector[] accountTypeSelectors;
  private Glob currentAccount;
  private LinkField accountType;
  private LinkField cardType;

  public static AccountTypeCombo create(GlobRepository repository) {
    return new AccountTypeCombo(repository, Account.ACCOUNT_TYPE, Account.CARD_TYPE) {

      public boolean isMain(Glob account) {
        return Account.isMain(account);
      }

      public boolean isSavings(Glob account) {
        return Account.isSavings(account);
      }
    };
  }

  public AccountTypeCombo(GlobRepository repository, LinkField accountType, LinkField cardType) {
    this.accountType = accountType;
    this.cardType = cardType;
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

  public void update(Glob account) {
    currentAccount = account;
    if (currentAccount != null) {
      for (AccountTypeSelector selector : accountTypeSelectors) {
        if (selector.isApplied(currentAccount)) {
          accountTypeCombo.setSelectedItem(selector);
          return;
        }
        accountTypeCombo.setSelectedIndex(-1);
      }
    }
  }

  protected AccountTypeSelector[] createTypeSelectors(final GlobRepository repository) {
    return new AccountTypeSelector[]{
      new AccountTypeSelector("account.type.main") {
        public void apply() {
          repository.update(currentAccount.getKey(), accountType, AccountType.MAIN.getId());
          repository.update(currentAccount.getKey(), cardType, AccountCardType.NOT_A_CARD.getId());
        }

        public boolean isApplied(Glob account) {
          return isMain(account) &&
                 account.get(cardType).equals(AccountCardType.NOT_A_CARD.getId());
        }
      },

      new AccountTypeSelector("accountCardType.credit") {
        public void apply() {
          repository.update(currentAccount.getKey(), accountType, AccountType.MAIN.getId());
          repository.update(currentAccount.getKey(), cardType, AccountCardType.CREDIT.getId());
        }

        public boolean isApplied(Glob account) {
          return isMain(account) &&
                 Utils.equal(account.get(cardType), AccountCardType.CREDIT.getId());
        }
      },
      new AccountTypeSelector("accountCardType.deferred") {
        public void apply() {
          repository.update(currentAccount.getKey(), accountType, AccountType.MAIN.getId());
          repository.update(currentAccount.getKey(), cardType, AccountCardType.DEFERRED.getId());
        }

        public boolean isApplied(Glob account) {
          return isMain(account) &&
                 Utils.equal(account.get(cardType), AccountCardType.DEFERRED.getId());
        }
      }
      ,

      new AccountTypeSelector("account.type.savings") {
        public void apply() {
          repository.update(currentAccount.getKey(), accountType, AccountType.SAVINGS.getId());
          repository.update(currentAccount.getKey(), cardType, AccountCardType.NOT_A_CARD.getId());
        }

        public boolean isApplied(Glob account) {
          return isSavings(account);
        }
      },
    };
  }

  public abstract boolean isMain(Glob account);

  public abstract boolean isSavings(Glob account);

  public void setEnabled(boolean editable) {
    accountTypeCombo.setEnabled(editable);
  }
}
