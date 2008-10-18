package org.designup.picsou.gui.monthsummary;

import org.designup.picsou.gui.View;
import org.designup.picsou.gui.description.Formatting;
import org.designup.picsou.gui.model.BalanceStat;
import org.designup.picsou.gui.model.SeriesStat;
import org.designup.picsou.gui.utils.AmountColors;
import org.designup.picsou.model.AccountBalanceLimit;
import org.designup.picsou.model.CurrentMonth;
import org.designup.picsou.model.Month;
import org.designup.picsou.model.Transaction;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.SelectionService;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.fields.DoubleField;
import org.globsframework.model.ChangeSet;
import org.globsframework.model.ChangeSetListener;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.format.GlobListStringifiers;
import org.globsframework.model.utils.GlobFieldComparator;
import org.globsframework.model.utils.GlobMatchers;
import org.globsframework.utils.directory.DefaultDirectory;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.util.Set;
import java.util.SortedSet;
import java.awt.*;

public class BalanceSummaryView extends View implements GlobSelectionListener {
  private SelectionService parentSelectionService;
  private JLabel balance;
  private JLabel total;
  private JPanel contentPanel;
  private JLabel amountSummaryLabel;
  private AmountColors amountColors;

  public BalanceSummaryView(GlobRepository repository, Directory parentDirectory) {
    super(repository, createDirectory(parentDirectory));
    parentSelectionService = parentDirectory.get(SelectionService.class);
    parentSelectionService.addListener(this, Month.TYPE);
    amountColors = new AmountColors(parentDirectory);
    repository.addChangeListener(new ChangeSetListener() {
      public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
        if (changeSet.containsChanges(SeriesStat.TYPE) ||
            changeSet.containsUpdates(Transaction.BALANCE) ||
            changeSet.containsUpdates(AccountBalanceLimit.LIMIT)) {
          updateDetails();
        }
      }

      public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
        if (changedTypes.contains(SeriesStat.TYPE) ||
            changedTypes.contains(Transaction.TYPE) ||
            changedTypes.contains(AccountBalanceLimit.TYPE)) {
          updateDetails();
        }
      }
    });
  }

  private static Directory createDirectory(Directory parentDirectory) {
    Directory directory = new DefaultDirectory(parentDirectory);
    directory.add(new SelectionService());
    return directory;
  }

  public void registerComponents(GlobsPanelBuilder parentBuilder) {
    GlobsPanelBuilder builder =
      new GlobsPanelBuilder(getClass(), "/layout/balanceSummaryView.splits", repository, directory);

    amountSummaryLabel = builder.add("amountSummaryLabel", new JLabel());
    total = builder.add("totalLabel", new JLabel());
    balance = builder.add("balanceLabel", new JLabel());
    addLabel(builder, "incomeLabel", BalanceStat.INCOME_REMAINING);
    addLabel(builder, "fixedLabel", BalanceStat.RECURRING_REMAINING);
    addLabel(builder, "savingsLabel", BalanceStat.SAVINGS_REMAINING);
    addLabel(builder, "specialLabel", BalanceStat.SPECIAL_REMAINING);
    addLabel(builder, "envelopeLabel", BalanceStat.ENVELOPES_REMAINING);
    addLabel(builder, "occasionalLabel", BalanceStat.OCCASIONAL_REMAINING);

    contentPanel = builder.add("content", new JPanel());
    contentPanel.setVisible(false);

    builder.add("accountBalanceLimit", new AccountBalanceLimitAction(repository, directory));

    parentBuilder.add("balanceSummaryView", builder);
  }

  private void addLabel(GlobsPanelBuilder builder, String name, DoubleField field) {
    builder.addLabel(name, BalanceStat.TYPE, GlobListStringifiers.sum(decimalFormat, field));
  }

  public void selectionUpdated(GlobSelection selection) {
    updateDetails();
  }

  private void updateDetails() {
    SortedSet<Integer> currentMonths = parentSelectionService.getSelection(Month.TYPE).getSortedSet(Month.ID);
    if (currentMonths.isEmpty()) {
      hide();
      return;
    }

    SortedSet<Glob> tmp = repository.getSorted(BalanceStat.TYPE, new GlobFieldComparator(BalanceStat.MONTH_ID),
                                               GlobMatchers.fieldIn(BalanceStat.MONTH_ID, currentMonths));

    Glob[] balanceStats = tmp.toArray(new Glob[tmp.size()]);
    if (balanceStats.length == 0) {
      hide();
      return;
    }
    selectionService.select(tmp, BalanceStat.TYPE);

    Glob currentMonth = repository.get(CurrentMonth.KEY);
    if (currentMonths.last() < currentMonth.get(CurrentMonth.MONTH_ID)) {
      contentPanel.setVisible(false);
      Glob balanceStat = balanceStats[balanceStats.length - 1];
      Double amount = balanceStat.get(BalanceStat.END_OF_MONTH_ACCOUNT_BALANCE);
      updateTotal(amount);
      amountSummaryLabel.setText(Lang.get("balanceSummary.title.past",
                                          Formatting.toString(
                                            Month.getLastDay(balanceStat.get(BalanceStat.MONTH_ID)))));
      return;
    }

    amountSummaryLabel.setText(Lang.get("balanceSummary.title.future",
                                        Formatting.toString(
                                          Month.getLastDay(balanceStats[balanceStats.length - 1].get(BalanceStat.MONTH_ID)))));
    Double amount;
    int firstBalanceIndex;
    for (firstBalanceIndex = 0; firstBalanceIndex < balanceStats.length; firstBalanceIndex++) {
      Glob balanceStat = balanceStats[firstBalanceIndex];
      if (balanceStat.get(BalanceStat.MONTH_ID) >= currentMonth.get(CurrentMonth.MONTH_ID)) {
        break;
      }
    }
    amount = balanceStats[firstBalanceIndex].get(BalanceStat.LAST_KNOWN_ACCOUNT_BALANCE);
    if (amount == null) {
      amount = balanceStats[firstBalanceIndex].get(BalanceStat.BEGIN_OF_MONTH_ACCOUNT_BALANCE);
    }
    String label = Formatting.toString(amount);
    balance.setText(label);

    for (Glob balance : balanceStats) {
      amount += balance.get(BalanceStat.ENVELOPES_REMAINING) +
                balance.get(BalanceStat.INCOME_REMAINING) +
                balance.get(BalanceStat.OCCASIONAL_REMAINING) +
                balance.get(BalanceStat.RECURRING_REMAINING) +
                balance.get(BalanceStat.SAVINGS_REMAINING) +
                balance.get(BalanceStat.SPECIAL_REMAINING);
    }

    updateTotal(amount);
    contentPanel.setVisible(true);
  }

  private void updateTotal(Double amount) {
    total.setText(Formatting.toString(amount));

    Double limit = AccountBalanceLimit.getLimit(repository);
    Color color = amountColors.get(amount - limit);
    total.setForeground(color);

    total.setVisible(true);
  }

  private void hide() {
    total.setVisible(false);
    total.setText("");
    contentPanel.setVisible(false);
  }

}
