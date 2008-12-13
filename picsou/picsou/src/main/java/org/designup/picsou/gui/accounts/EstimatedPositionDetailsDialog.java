package org.designup.picsou.gui.accounts;

import org.designup.picsou.gui.components.CloseAction;
import org.designup.picsou.gui.components.PicsouDialog;
import org.designup.picsou.gui.description.Formatting;
import org.designup.picsou.gui.model.BalanceStat;
import org.designup.picsou.model.CurrentMonth;
import org.designup.picsou.model.Month;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.SelectionService;
import org.globsframework.metamodel.fields.DoubleField;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.model.format.GlobListStringifier;
import org.globsframework.model.format.GlobListStringifiers;
import org.globsframework.utils.directory.DefaultDirectory;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;

public class EstimatedPositionDetailsDialog {
  private JLabel amountSummaryLabel;
  private Directory directory;
  private GlobRepository repository;
  private PicsouDialog dialog;

  public EstimatedPositionDetailsDialog(GlobRepository repository, Directory parentDirectory) {
    this.repository = repository;
    this.directory = createDirectory(parentDirectory);
    createDialog();
  }

  public void show(GlobList selectedMonths) {
    GlobList stats = new GlobList();
    for (Glob month : selectedMonths) {
      stats.add(repository.find(Key.create(BalanceStat.TYPE, month.get(Month.ID))));
    }
    directory.get(SelectionService.class).select(stats, BalanceStat.TYPE);
    dialog.showCentered();
  }

  private static Directory createDirectory(Directory parentDirectory) {
    Directory directory = new DefaultDirectory(parentDirectory);
    directory.add(new SelectionService());
    return directory;
  }

  public void createDialog() {
    GlobsPanelBuilder builder =
      new GlobsPanelBuilder(getClass(), "/layout/estimatedPositionDetailsDialog.splits", repository, directory);

    builder.addLabel("estimatedPosition", BalanceStat.TYPE, new EspectedPositionStringifier()).getComponent();
    builder.addLabel("estimatedPositionDate", BalanceStat.TYPE, new PositionDateStringifier()).getComponent();
    builder.addLabel("initialPosition", BalanceStat.TYPE, new InitialPositionStringifier()).getComponent();
    addLabel(builder, "remainingIncome", BalanceStat.INCOME_REMAINING);
    addLabel(builder, "remainingFixed", BalanceStat.RECURRING_REMAINING);
    addLabel(builder, "remainingEnvelope", BalanceStat.ENVELOPES_REMAINING);
    addLabel(builder, "remainingSavings", BalanceStat.SAVINGS_REMAINING);
    addLabel(builder, "remainingSpecial", BalanceStat.SPECIAL_REMAINING);
    addLabel(builder, "remainingOccasional", BalanceStat.OCCASIONAL_REMAINING);

    JPanel panel = builder.load();

    dialog = PicsouDialog.create(directory.get(JFrame.class), true, directory);
    dialog.setPanelAndButton(panel, new CloseAction(dialog));
    dialog.pack();
  }

  private void addLabel(GlobsPanelBuilder builder, String name, DoubleField field) {
    builder.addLabel(name, BalanceStat.TYPE, GlobListStringifiers.sum(Formatting.DECIMAL_FORMAT, field));
  }

  private class EspectedPositionStringifier implements GlobListStringifier {
    public String toString(GlobList list, GlobRepository repository) {
      Glob balanceStat = getLastBalanceStat(list);
      if (balanceStat == null) {
        return "";
      }
      return Formatting.toString(balanceStat.get(BalanceStat.END_OF_MONTH_ACCOUNT_POSITION));
    }
  }

  private class PositionDateStringifier implements GlobListStringifier {
    public String toString(GlobList list, GlobRepository repository) {
      Glob balanceStat = getLastBalanceStat(list);
      if (balanceStat == null) {
        return "";
      }
      return Formatting.toString(Month.getLastDay(balanceStat.get(BalanceStat.MONTH)));
    }
//<<<<<<< local
//
//    SortedSet<Glob> tmp = repository.getSorted(BalanceStat.TYPE, new GlobFieldComparator(BalanceStat.MONTH),
//                                               GlobMatchers.fieldIn(BalanceStat.MONTH, currentMonths));
//
//    Glob[] balanceStats = tmp.toArray(new Glob[tmp.size()]);
//    if (balanceStats.length == 0) {
//      hide();
//      return;
//    }
//    selectionService.select(tmp, BalanceStat.TYPE);
//
//    Glob currentMonth = repository.get(CurrentMonth.KEY);
//    if (currentMonths.last() < currentMonth.get(CurrentMonth.LAST_TRANSACTION_MONTH)) {
//      contentPanel.setVisible(false);
//      Glob balanceStat = balanceStats[balanceStats.length - 1];
//      Double amount = balanceStat.get(BalanceStat.END_OF_MONTH_ACCOUNT_POSITION);
//      if (amount != null) {
//        updateTotal(amount);
//        amountSummaryLabel.setText(Lang.get("balanceSummary.title.past",
//                                            Formatting.toString(
//                                              Month.getLastDay(balanceStat.get(BalanceStat.MONTH)))));
//      }
//      return;
//    }
//
//    amountSummaryLabel.setText(Lang.get("balanceSummary.title.future",
//                                        Formatting.toString(
//                                          Month.getLastDay(balanceStats[balanceStats.length - 1].get(BalanceStat.MONTH)))));
//    Double amount;
//    int firstBalanceIndex;
//    for (firstBalanceIndex = 0; firstBalanceIndex < balanceStats.length; firstBalanceIndex++) {
//      Glob balanceStat = balanceStats[firstBalanceIndex];
//      if (balanceStat.get(BalanceStat.MONTH) >= currentMonth.get(CurrentMonth.LAST_TRANSACTION_MONTH)) {
//        break;
//      }
//    }
//    amount = balanceStats[firstBalanceIndex].get(BalanceStat.LAST_KNOWN_ACCOUNT_POSITION);
//    if (amount == null) {
//      amount = balanceStats[firstBalanceIndex].get(BalanceStat.BEGIN_OF_MONTH_ACCOUNT_POSITION);
//    }
//    if (amount == null) {
//      contentPanel.setVisible(false);
//      return;
//    }
//    String label = Formatting.toString(amount);
//    balance.setVisible(true);
//    balance.setText(label);
//
//    for (Glob balance : balanceStats) {
//      amount += balance.get(BalanceStat.ENVELOPES_REMAINING) +
//                balance.get(BalanceStat.INCOME_REMAINING) +
//                balance.get(BalanceStat.OCCASIONAL_REMAINING) +
//                balance.get(BalanceStat.RECURRING_REMAINING) +
//                balance.get(BalanceStat.SAVINGS_REMAINING) +
//                balance.get(BalanceStat.SPECIAL_REMAINING);
//    }
//
//    updateTotal(amount);
//    contentPanel.setVisible(true);
//=======
//>>>>>>> other
  }

  private Glob getLastBalanceStat(GlobList list) {
    list.sort(BalanceStat.MONTH);
    return list.getLast();
  }

  private class InitialPositionStringifier implements GlobListStringifier {
    public String toString(GlobList balanceStats, GlobRepository repository) {
      balanceStats.sort(BalanceStat.MONTH);
      if (balanceStats.isEmpty()) {
        return "";
      }

      Integer currentMonthId = CurrentMonth.get(repository);
      Glob balanceStat = null;
      for (Glob stat : balanceStats) {
        balanceStat = stat;
        if (balanceStat.get(BalanceStat.MONTH) >= currentMonthId) {
          break;
        }
      }
      if (balanceStat == null) {
        return "";
      }

      Double amount = balanceStat.get(BalanceStat.LAST_KNOWN_ACCOUNT_POSITION);
      if (amount == null) {
        amount = balanceStat.get(BalanceStat.BEGIN_OF_MONTH_ACCOUNT_POSITION);
      }
      return Formatting.toString(amount);
    }
  }
}