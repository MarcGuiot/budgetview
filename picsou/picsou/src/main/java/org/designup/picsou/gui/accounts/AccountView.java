package org.designup.picsou.gui.accounts;

import org.designup.picsou.gui.View;
import org.designup.picsou.gui.actions.ImportFileAction;
import org.designup.picsou.gui.browsing.BrowsingService;
import org.designup.picsou.gui.description.Formatting;
import org.designup.picsou.model.Account;
import org.designup.picsou.model.Bank;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.splits.repeat.RepeatCellBuilder;
import org.globsframework.gui.splits.repeat.RepeatComponentFactory;
import org.globsframework.gui.views.AbstractGlobTextView;
import org.globsframework.gui.views.GlobButtonView;
import org.globsframework.gui.views.GlobLabelView;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.model.format.GlobListStringifier;
import org.globsframework.model.utils.GlobListFunctor;
import static org.globsframework.model.utils.GlobMatchers.*;
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
    builder.addLabel("accountTotalTitle", Account.TYPE, new TotalAmountStringifier())
      .setAutoHideIfEmpty(true)
      .forceSelection(summaryAccount);

    builder.addLabel("totalBalance", Account.BALANCE)
      .setAutoHideIfEmpty(true)
      .forceSelection(summaryAccount);

    builder.add("createAccount", new AbstractAction() {

      public void actionPerformed(ActionEvent e) {
        AccountEditionDialog accountEditionDialog =
          new AccountEditionDialog(directory.get(JFrame.class), repository, directory);
        accountEditionDialog.showWithNewAccount();
      }
    });

    builder.addRepeat("accountRepeat", Account.TYPE, not(contains(summaryAccount)),
                      new AccountComparator(),
                      new AccountRepeatFactory());

    parentBuilder.add("accountView", builder);
  }

  private class AccountRepeatFactory implements RepeatComponentFactory<Glob> {
    public void registerComponents(RepeatCellBuilder cellBuilder, final Glob account) {
      add("accountName",
          GlobButtonView.init(Account.NAME, repository, directory, new EditAccountFunctor()),
          account, cellBuilder);

      add("accountNumber",
          GlobLabelView.init(Account.NUMBER, repository, directory),
          account, cellBuilder);

      add("accountUpdateDate",
          GlobLabelView.init(Account.BALANCE_DATE, repository, directory),
          account, cellBuilder);

      final GlobButtonView balance =
        GlobButtonView.init(Account.TYPE, repository, directory,
                            new GlobListStringifier() {
                              public String toString(GlobList list, GlobRepository repository) {
                                if (list.isEmpty()) {
                                  return "";
                                }
                                Double balance = list.get(0).get(Account.BALANCE);
                                if (balance == null) {
                                  return "0.0";
                                }
                                return decimalFormat.format(balance);
                              }
                            },
                            new GlobListFunctor() {
                              public void run(GlobList list, GlobRepository repository) {
                                BalanceEditionDialog balanceEditor =
                                  new BalanceEditionDialog(account, false, repository, directory, directory.get(JFrame.class));
                                balanceEditor.show();
                              }
                            }).forceSelection(account);
      cellBuilder.add("accountBalance", balance.getComponent());

      cellBuilder.add("gotoWebsite", new GotoWebsiteAction(account));
      cellBuilder.add("importData", ImportFileAction.init(Lang.get("account.import.data"), repository, directory, account));
      cellBuilder.addDisposeListener(balance);
    }

    private void add(String name, final AbstractGlobTextView labelView, Glob account, RepeatCellBuilder cellBuilder) {
      labelView.forceSelection(account);
      cellBuilder.add(name, labelView.getComponent());
      cellBuilder.addDisposeListener(labelView);
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

    private class EditAccountFunctor implements GlobListFunctor {
      public void run(GlobList list, GlobRepository repository) {
        AccountEditionDialog dialog = new AccountEditionDialog(directory.get(JFrame.class), repository, directory);
        dialog.show(list.get(0));
      }
    }
  }

  private static class TotalAmountStringifier implements GlobListStringifier {
    public String toString(GlobList list, GlobRepository repository) {
      if (list.isEmpty() || list.get(0).get(Account.BALANCE_DATE) == null) {
        return "";
      }
      return Lang.get("account.total.title", Formatting.toString(list.get(0).get(Account.BALANCE_DATE)));
    }
  }
}
