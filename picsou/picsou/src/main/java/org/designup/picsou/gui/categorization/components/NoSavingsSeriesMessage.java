package org.designup.picsou.gui.categorization.components;

import org.designup.picsou.model.Account;
import org.designup.picsou.model.BudgetArea;
import org.designup.picsou.model.AccountType;
import org.designup.picsou.model.AccountUpdateMode;
import org.designup.picsou.gui.utils.Matchers;
import org.designup.picsou.gui.accounts.AccountEditionDialog;
import org.designup.picsou.utils.Lang;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.ChangeSet;
import org.globsframework.model.ChangeSetListener;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import java.util.Set;

public class NoSavingsSeriesMessage extends NoSeriesMessage {
  public NoSavingsSeriesMessage(GlobRepository repository, Directory directory) {
    super(BudgetArea.SAVINGS, repository, directory);

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
    if ("createSavingsAccount".equals(href)) {
      AccountEditionDialog dialog = new AccountEditionDialog(repository, directory);
      dialog.showWithNewAccount(AccountType.SAVINGS, AccountUpdateMode.AUTOMATIC, true);
    }
  }

  private void updateMessage() {
    if (!repository.contains(Account.TYPE, Matchers.userCreatedSavingsAccounts())) {
      setText(Lang.get("categorization.noseries.savings.noaccount"));
    }
    else {
      setDefaultText();
    }
  }
}
