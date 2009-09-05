package org.designup.picsou.gui.accounts;

import org.designup.picsou.gui.browsing.BrowsingService;
import org.designup.picsou.gui.description.AccountComparator;
import org.designup.picsou.gui.description.Formatting;
import org.designup.picsou.gui.monthsummary.AccountPositionThresholdAction;
import org.designup.picsou.gui.utils.PicsouMatchers;
import org.designup.picsou.model.*;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.SelectionService;
import org.globsframework.gui.utils.GlobRepeat;
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
import org.globsframework.model.utils.GlobMatcher;
import org.globsframework.model.utils.GlobMatchers;
import org.globsframework.utils.Strings;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;

public abstract class AccountViewPanel {
  protected GlobRepository repository;
  protected Directory directory;
  private GlobMatcher accountTypeMatcher;
  private GlobMatcher filterMatcherWithDates;
  private Integer summaryId;
  private JPanel panel;
  private JPanel header;
  private JLabel labelTypeName;
  private GlobRepeat accountRepeat;

  public AccountViewPanel(final GlobRepository repository, final Directory directory,
                          GlobMatcher accountMatcher, Integer summaryId) {
    this.repository = repository;
    this.directory = directory;
    this.accountTypeMatcher = accountMatcher;
    this.summaryId = summaryId;
    directory.get(SelectionService.class).addListener(new GlobSelectionListener() {
      public void selectionUpdated(GlobSelection selection) {
        GlobList months = selection.getAll(Month.TYPE);
        filterMatcherWithDates =
          GlobMatchers.and(accountTypeMatcher,
                           new PicsouMatchers.AccountDateMatcher(months));
        accountRepeat.setFilter(filterMatcherWithDates);
      }
    }, Month.TYPE);
  }

  private void createPanel() {
    GlobsPanelBuilder builder =
      new GlobsPanelBuilder(getClass(), "/layout/accountViewPanel.splits", repository, directory);

    header = builder.add("header", new JPanel()).getComponent();

    Key summaryAccount = Key.create(Account.TYPE, summaryId);
    registerSummaryView(builder);

    builder.addLabel("referencePosition", Account.POSITION)
      .setAutoHideIfEmpty(true)
      .forceSelection(summaryAccount);
    builder.addLabel("referencePositionDate", Account.TYPE, new ReferenceAmountStringifier())
      .setAutoHideIfEmpty(true)
      .forceSelection(summaryAccount);

    labelTypeName = new JLabel();
    builder.add("labelTypeName", labelTypeName);

    accountRepeat = builder.addRepeat("accountRepeat", Account.TYPE, accountTypeMatcher,
                      new AccountComparator(),
                      new AccountRepeatFactory());

    builder.add("createAccount",
                new NewAccountAction(getAccountType(), repository, directory, directory.get(JFrame.class)));

    AccountPositionThresholdAction action = new AccountPositionThresholdAction(repository, directory);
    action.setEnabled(showPositionThreshold());
    builder.add("accountPositionThreshold", action);

    panel = builder.load();
  }

  protected abstract void registerSummaryView(GlobsPanelBuilder builder);

  protected abstract AccountType getAccountType();

  protected abstract boolean showPositionThreshold();

  public JPanel getPanel() {
    if (panel == null) {
      createPanel();
    }
    return panel;
  }

  protected void updateEstimatedPosition() {

    boolean hasAccounts = !repository.getAll(Account.TYPE, accountTypeMatcher).isEmpty();
    header.setVisible(hasAccounts);
    if (!hasAccounts) {
      return;
    }
  }

  protected abstract JLabel getEstimatedAccountPositionLabel(Key accountKey);

  protected abstract JLabel getEstimatedAccountPositionDateLabel(Key accountKey);

  private class AccountRepeatFactory implements RepeatComponentFactory<Glob> {
    public void registerComponents(RepeatCellBuilder cellBuilder, final Glob account) {
      add("accountName",
          createAccountNameButton(account, repository, directory),
          account, cellBuilder);

      add("accountNumber",
          GlobLabelView.init(Account.NUMBER, repository, directory),
          account, cellBuilder);

      add("accountUpdateDate",
          GlobLabelView.init(Account.POSITION_DATE, repository, directory),
          account, cellBuilder);

      final GlobButtonView balance =
        GlobButtonView.init(Account.TYPE, repository, directory,
                            new GlobListStringifier() {
                              public String toString(GlobList list, GlobRepository repository) {
                                if (list.isEmpty()) {
                                  return "";
                                }
                                Double position = list.get(0).get(Account.POSITION);
                                if (position == null) {
                                  return "0.0";
                                }
                                return Formatting.DECIMAL_FORMAT.format(position);
                              }
                            },
                            new GlobListFunctor() {
                              public void run(GlobList list, GlobRepository repository) {
                                AccountPositionEditionDialog accountPositionEditor =
                                  new AccountPositionEditionDialog(account, false, repository, directory, directory.get(JFrame.class));
                                accountPositionEditor.show();
                              }
                            }).forceSelection(account.getKey());
      cellBuilder.add("accountPosition", balance.getComponent());

      cellBuilder.add("estimatedAccountPosition", getEstimatedAccountPositionLabel(account.getKey()));
      cellBuilder.add("estimatedAccountPositionDate", getEstimatedAccountPositionDateLabel(account.getKey()));

      cellBuilder.addDisposeListener(balance);
    }

    private void add(String name, final AbstractGlobTextView labelView, Glob account, RepeatCellBuilder cellBuilder) {
      labelView.forceSelection(account.getKey());
      cellBuilder.add(name, labelView.getComponent());
      cellBuilder.addDisposeListener(labelView);
    }

    private class GotoWebsiteAction extends AbstractAction {
      private String url;

      public GotoWebsiteAction(Glob account) {
        super(Lang.get("accountView.goto.website"));
        url = Account.getBank(account, repository).get(Bank.DOWNLOAD_URL);
        setEnabled(Strings.isNotEmpty(url));
      }

      public void actionPerformed(ActionEvent e) {
        directory.get(BrowsingService.class).launchBrowser(url);
      }
    }
  }

  private static class EditAccountFunctor implements GlobListFunctor {

    private GlobRepository repository;
    private Directory directory;

    private EditAccountFunctor(GlobRepository repository, Directory directory) {
      this.repository = repository;
      this.directory = directory;
    }

    public void run(GlobList list, GlobRepository repository) {
      AccountEditionDialog dialog = new AccountEditionDialog(directory.get(JFrame.class), repository, directory);
      dialog.show(list.get(0));
    }
  }

  private static class ReferenceAmountStringifier implements GlobListStringifier {

    public String toString(GlobList list, GlobRepository repository) {
      if (list.isEmpty() || list.get(0).get(Account.POSITION_DATE) == null) {
        return "";
      }
      return Lang.get("accountView.total.date", Formatting.toString(list.get(0).get(Account.POSITION_DATE)));
    }
  }

  public static GlobButtonView createAccountNameButton(Glob account, final GlobRepository repository, final Directory directory) {
    return GlobButtonView.init(Account.NAME, repository, directory,
                               new EditAccountFunctor(repository, directory)).forceSelection(account.getKey());
  }
}