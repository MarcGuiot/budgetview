package org.designup.picsou.gui.accounts;

import org.designup.picsou.gui.View;
import org.designup.picsou.gui.actions.ImportFileAction;
import org.designup.picsou.gui.browsing.BrowsingService;
import org.designup.picsou.model.Account;
import org.designup.picsou.model.Bank;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.splits.repeat.RepeatCellBuilder;
import org.globsframework.gui.splits.repeat.RepeatComponentFactory;
import org.globsframework.gui.views.GlobLabelView;
import org.globsframework.metamodel.Field;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import static org.globsframework.model.utils.GlobMatchers.contains;
import static org.globsframework.model.utils.GlobMatchers.not;
import org.globsframework.utils.Strings;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class AccountView extends View {

  public AccountView(GlobRepository repository, Directory directory) {
    super(repository, directory);
  }

  public void registerComponents(GlobsPanelBuilder parentBuilder) {

    GlobsPanelBuilder builder = new GlobsPanelBuilder(getClass(), "/layout/accountView.splits", repository, directory);

    Glob summaryAccount = repository.get(Key.create(Account.TYPE, Account.SUMMARY_ACCOUNT_ID));
    builder.addLabel("totalBalance", Account.BALANCE).forceSelection(summaryAccount);

    builder.addRepeat("accountRepeat", Account.TYPE, not(contains(summaryAccount)),
                      new AccountComparator(),
                      new AccountRepeatFactory());

    parentBuilder.add("accountView", builder);
  }

  private class AccountRepeatFactory implements RepeatComponentFactory<Glob> {
    public void registerComponents(RepeatCellBuilder cellBuilder, Glob account) {
      add("accountName", Account.NAME, account, cellBuilder);
      add("accountNumber", Account.NUMBER, account, cellBuilder);
      add("accountBalance", Account.BALANCE, account, cellBuilder);
      add("accountUpdateDate", Account.UPDATE_DATE, account, cellBuilder);

      cellBuilder.add("gotoWebsite", new GotoWebsiteAction(account));
      cellBuilder.add("importData", ImportFileAction.init(Lang.get("account.import.data"), repository, directory, account));
    }

    private void add(String name, Field field, Glob account, RepeatCellBuilder cellBuilder) {
      final GlobLabelView globLabelView = GlobLabelView.init(field, repository, directory);
      cellBuilder.add(name, globLabelView
        .forceSelection(account)
        .getComponent());
      cellBuilder.addDisposeListener(new RepeatCellBuilder.DisposeListener() {
        public void dispose() {
          globLabelView.dispose();
        }
      });
    }

    private class GotoWebsiteAction extends AbstractAction {
      private String url;

      public GotoWebsiteAction(Glob account) {
        super(Lang.get("account.goto.website"));
        url = Account.getBank(account, repository).get(Bank.DOWNLOAD_URL);
        setEnabled(Strings.isNotEmpty(url));
      }

      public void actionPerformed(ActionEvent e) {

        directory.get(BrowsingService.class).launchBrowser(url);
      }
    }
  }
}
