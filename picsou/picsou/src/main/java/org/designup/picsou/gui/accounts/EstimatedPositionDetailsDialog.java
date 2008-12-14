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

      Integer currentMonthId = CurrentMonth.getLastTransactionMonth(repository);
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