package com.budgetview.desktop.categorization.components.messages;

import com.budgetview.desktop.accounts.AccountEditionDialog;
import com.budgetview.desktop.accounts.utils.AccountMatchers;
import com.budgetview.model.Account;
import com.budgetview.shared.model.AccountType;
import com.budgetview.model.AccountUpdateMode;
import com.budgetview.shared.model.BudgetArea;
import com.budgetview.utils.Lang;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.ChangeSet;
import org.globsframework.model.ChangeSetListener;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import java.util.Set;

public class NoSavingsSeriesMessage extends NoSeriesMessage {
  public NoSavingsSeriesMessage(GlobRepository repository, Directory directory) {
    super(BudgetArea.TRANSFER, repository, directory);

    updateMessage();

    repository.addChangeListener(new ChangeSetListener() {
      public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
        if (changeSet.containsCreationsOrDeletions(Account.TYPE)) {
          updateMessage();
        }
      }

      public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
        if (changedTypes.contains(Account.TYPE)) {
          updateMessage();
        }
      }
    });
  }

  protected void processHyperlinkClick(String href) {
    if ("createAccount".equals(href)) {
      AccountEditionDialog dialog = new AccountEditionDialog(repository, directory, true);
      dialog.showWithNewAccount(AccountType.MAIN, true, AccountUpdateMode.MANUAL);
    }
  }

  private void updateMessage() {
    if (!repository.contains(Account.TYPE, AccountMatchers.userCreatedAccounts())) {
      setText(Lang.get("categorization.noseries.transfer.noaccount"));
    }
    else {
      setDefaultText();
    }
  }
}
