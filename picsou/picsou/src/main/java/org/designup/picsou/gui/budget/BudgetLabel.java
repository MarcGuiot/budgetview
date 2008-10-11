package org.designup.picsou.gui.budget;

import org.designup.picsou.gui.description.ColoredAmountStringifier;
import org.designup.picsou.gui.description.PicsouDescriptionService;
import org.designup.picsou.gui.model.BalanceStat;
import org.designup.picsou.model.Month;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.SelectionService;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.ChangeSet;
import org.globsframework.model.ChangeSetListener;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.utils.GlobMatchers;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.text.DecimalFormat;
import java.util.Set;

public class BudgetLabel implements GlobSelectionListener, ChangeSetListener {

  private JLabel label = new JLabel();
  private SelectionService selectionService;
  private GlobRepository repository;
  private ColoredAmountStringifier monthBalanceStringifier;
  private DecimalFormat format = PicsouDescriptionService.DECIMAL_FORMAT;

  public BudgetLabel(GlobRepository repository, Directory directory) {
    this.repository = repository;
    repository.addChangeListener(this);
    selectionService = directory.get(SelectionService.class);
    selectionService.addListener(this, Month.TYPE);

    monthBalanceStringifier = new ColoredAmountStringifier(BalanceStat.MONTH_BALANCE, true, directory);

    update();
  }

  public JLabel getLabel() {
    return label;
  }

  public void update() {

    Set<Integer> selectedMonthIds = selectionService.getSelection(Month.TYPE).getValueSet(Month.ID);

    GlobList balanceStats =
      repository.getAll(BalanceStat.TYPE, GlobMatchers.fieldIn(BalanceStat.MONTH_ID, selectedMonthIds))
        .sort(BalanceStat.MONTH_ID);

    if (balanceStats.size() == 0) {
      label.setText("");
      return;
    }

    StringBuilder builder = new StringBuilder();
    builder.append("<html>");

    if (selectedMonthIds.size() > 1) {
      builder
        .append("<b>")
        .append(Lang.get("budgetLabel.multimonth", selectedMonthIds.size()))
        .append("</b> - ");
    }

    builder
      .append(Lang.get("budgetLabel.monthBalance")).append(" <b>")
      .append(monthBalanceStringifier.toString(balanceStats, repository))
      .append("</b> - ")
      .append(Lang.get("budgetLabel.endBalance")).append(" <b>")
      .append(format.format(balanceStats.getLast().get(BalanceStat.END_OF_MONTH_ACCOUNT_BALANCE)))
      .append("</b>");

    double uncategorizedTotal = balanceStats.getSum(BalanceStat.UNCATEGORIZED);
    if (uncategorizedTotal != 0) {
      String uncategorized = format.format(uncategorizedTotal);
      builder
        .append(" - ")
        .append(Lang.get("budgetLabel.uncategorized")).append(" <b>")
        .append(uncategorized)
        .append("</b>");
    }

    builder.append("</html>");

    label.setText(builder.toString());
  }

  public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
    if (changeSet.containsChanges(BalanceStat.TYPE)) {
      update();
    }
  }

  public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
    if (changedTypes.contains(BalanceStat.TYPE)) {
      update();
    }
  }

  public void selectionUpdated(GlobSelection selection) {
    update();
  }
}
