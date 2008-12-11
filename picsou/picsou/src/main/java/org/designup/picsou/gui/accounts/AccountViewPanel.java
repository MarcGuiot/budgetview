package org.designup.picsou.gui.accounts;

import org.designup.picsou.gui.actions.ImportFileAction;
import org.designup.picsou.gui.browsing.BrowsingService;
import org.designup.picsou.gui.description.AccountComparator;
import org.designup.picsou.gui.description.Formatting;
import org.designup.picsou.gui.monthsummary.AccountPositionThresholdAction;
import org.designup.picsou.model.Account;
import org.designup.picsou.model.Bank;
import org.designup.picsou.model.Month;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.SelectionService;
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
import org.globsframework.model.format.GlobPrinter;
import org.globsframework.model.utils.GlobListFunctor;
import org.globsframework.model.utils.GlobMatcher;
import org.globsframework.utils.Strings;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;

public abstract class AccountViewPanel {
  protected GlobRepository repository;
  protected Directory directory;
  private GlobMatcher accountMatcher;
  private Integer summaryId;
  private JPanel panel;
  private JPanel header;

  public AccountViewPanel(final GlobRepository repository, final Directory directory,
                          GlobMatcher accountMatcher, Integer summaryId) {
    this.repository = repository;
    this.directory = directory;
    this.accountMatcher = accountMatcher;
    this.summaryId = summaryId;
    directory.get(SelectionService.class).addListener(new GlobSelectionListener() {
      public void selectionUpdated(GlobSelection selection) {
        updateEstimatedPosition();
      }
    }, Month.TYPE);
  }

  private void createPanel() {
    GlobsPanelBuilder builder =
      new GlobsPanelBuilder(getClass(), "/layout/accountViewPanel.splits", repository, directory);

    header = builder.add("header", new JPanel());

    Glob summaryAccount = repository.get(Key.create(Account.TYPE, summaryId));
    builder.addLabel("referencePosition", Account.BALANCE)
      .setAutoHideIfEmpty(true)
      .forceSelection(summaryAccount);
    builder.addLabel("referencePositionDate", Account.TYPE, new ReferenceAmountStringifier())
      .setAutoHideIfEmpty(true)
      .forceSelection(summaryAccount);

    builder.add("estimatedPosition", getEstimatedPositionComponent());
    builder.add("estimatedPositionDate", getEstimatedPositionDateComponent());

    builder.addRepeat("accountRepeat", Account.TYPE, accountMatcher,
                      new AccountComparator(),
                      new AccountRepeatFactory());

    builder.add("createAccount", new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
        AccountEditionDialog accountEditionDialog =
          new AccountEditionDialog(directory.get(JFrame.class), repository, directory);
        accountEditionDialog.showWithNewAccount();
      }
    });

    AccountPositionThresholdAction action = new AccountPositionThresholdAction(repository, directory);
    action.setEnabled(showPositionThreshold());
    builder.add("accountPositionThreshold", action);

    panel = builder.load();

    updateEstimatedPosition();
  }

  protected abstract boolean showPositionThreshold();

  protected abstract JComponent getEstimatedPositionComponent();

  protected abstract JComponent getEstimatedPositionDateComponent();

  public JPanel getPanel() {
    if (panel == null) {
      createPanel();
    }
    return panel;
  }

  protected void updateEstimatedPosition() {

    boolean hasAccounts = !repository.getAll(Account.TYPE, accountMatcher).isEmpty();
    header.setVisible(hasAccounts);
    getEstimatedPositionComponent().setVisible(hasAccounts);
    if (!hasAccounts) {
      return;
    }

    GlobList list = directory.get(SelectionService.class).getSelection(Month.TYPE);
    if (list.isEmpty()) {
      setEstimatedPositionLabels(null, "");
      return;
    }
    list.sort(Month.ID);
    Integer lastSelectedMonthId = list.getLast().get(Month.ID);

    Glob balanceStat = getBalanceStat(lastSelectedMonthId);
    if (balanceStat == null) {
      setEstimatedPositionLabels(null, "");
      return;
    }

    String lastDay = Formatting.toString(Month.getLastDay(lastSelectedMonthId));
    String dateLabel = Lang.get("accountView.total.date", lastDay);

    Double amount = getEndOfMonthPosition(balanceStat);
    setEstimatedPositionLabels(amount, dateLabel);
  }

  protected abstract Double getEndOfMonthPosition(Glob balanceStat);

  protected abstract Glob getBalanceStat(Integer lastSelectedMonthId);

  protected abstract void setEstimatedPositionLabels(Double amount, String date);

  protected abstract JLabel getEstimatedAccountPositionLabel(Glob account);

  protected abstract JLabel getEstimatedAccountPositionDateLabel(Glob account);

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
                                return Formatting.DECIMAL_FORMAT.format(balance);
                              }
                            },
                            new GlobListFunctor() {
                              public void run(GlobList list, GlobRepository repository) {
                                AccountPositionEditionDialog accountPositionEditor =
                                  new AccountPositionEditionDialog(account, false, repository, directory, directory.get(JFrame.class));
                                accountPositionEditor.show();
                              }
                            }).forceSelection(account);
      cellBuilder.add("accountPosition", balance.getComponent());

      cellBuilder.add("estimatedAccountPosition", getEstimatedAccountPositionLabel(account));
      cellBuilder.add("estimatedAccountPositionDate", getEstimatedAccountPositionDateLabel(account));

      cellBuilder.add("gotoWebsite", new GotoWebsiteAction(account));
      cellBuilder.add("importData", ImportFileAction.init(Lang.get("accountView.import.data"), repository, directory, account));
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
        super(Lang.get("accountView.goto.website"));
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

  private static class ReferenceAmountStringifier implements GlobListStringifier {

    public String toString(GlobList list, GlobRepository repository) {
      if (list.isEmpty() || list.get(0).get(Account.BALANCE_DATE) == null) {
        return "";
      }
      return Lang.get("accountView.total.date", Formatting.toString(list.get(0).get(Account.BALANCE_DATE)));
    }
  }
}