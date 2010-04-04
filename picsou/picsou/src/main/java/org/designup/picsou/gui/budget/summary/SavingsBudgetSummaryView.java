package org.designup.picsou.gui.budget.summary;

import org.designup.picsou.gui.View;
import org.designup.picsou.gui.description.Formatting;
import org.designup.picsou.gui.model.SavingsBudgetStat;
import org.designup.picsou.model.Account;
import org.designup.picsou.model.Month;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.splits.color.ColorChangeListener;
import org.globsframework.gui.splits.color.ColorLocator;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.ChangeSet;
import org.globsframework.model.ChangeSetListener;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import static org.globsframework.model.utils.GlobMatchers.*;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.*;
import java.text.DecimalFormat;
import java.util.Set;
import java.util.SortedSet;

public class SavingsBudgetSummaryView extends View implements GlobSelectionListener, ChangeSetListener, ColorChangeListener {
  private JLabel balanceLabel = new JLabel();
  private JLabel positionTitle = new JLabel();
  private JLabel positionLabel = new JLabel();
  private JLabel multiSelectionLabel = new JLabel();
  private final DecimalFormat format = Formatting.DECIMAL_FORMAT;

  private Color normalColor;
  private Color errorColor;

  public SavingsBudgetSummaryView(GlobRepository repository, Directory directory) {
    super(repository, directory);
    repository.addChangeListener(this);
    selectionService.addListener(this, Month.TYPE);
    colorService.addListener(this);
    update();
  }

  public void colorsChanged(ColorLocator colorLocator) {
    normalColor = colorLocator.get("block.total");
    errorColor = colorLocator.get("block.total.error");
  }

  public void registerComponents(GlobsPanelBuilder parentBuilder) {
    GlobsPanelBuilder builder = new GlobsPanelBuilder(getClass(), "/layout/savings/savingsBudgetSummaryView.splits",
                                                      repository, directory);
    builder.add("balanceLabel", balanceLabel);
    builder.add("positionTitle", positionTitle);
    builder.add("positionLabel", positionLabel);
    builder.add("multiSelectionLabel", multiSelectionLabel);

    parentBuilder.add("budgetSummaryView", builder);
  }

  public void update() {

    SortedSet<Integer> selectedMonthIds = selectionService.getSelection(Month.TYPE).getSortedSet(Month.ID);
    if (selectedMonthIds.size() > 1) {
      multiSelectionLabel.setText(Lang.get("budgetSummaryView.multimonth", selectedMonthIds.size()));
      multiSelectionLabel.setVisible(true);
    }
    else {
      multiSelectionLabel.setVisible(false);
    }

    if (selectedMonthIds.isEmpty()) {
      positionTitle.setText(Lang.get("budgetSummaryView.position"));
    }
    else {
      Integer lastSelectedMonthId = selectedMonthIds.last();
      positionTitle.setText(getShortDate(lastSelectedMonthId));
    }

    GlobList budgetStats =
      repository.getAll(SavingsBudgetStat.TYPE,
                        and(fieldEquals(SavingsBudgetStat.ACCOUNT, Account.SAVINGS_SUMMARY_ACCOUNT_ID),
                            fieldIn(SavingsBudgetStat.MONTH, selectedMonthIds)))
        .sort(SavingsBudgetStat.MONTH);

    if (budgetStats.size() == 0) {
      clear(balanceLabel);
      clear(positionLabel);
      return;
    }

    Double balance = budgetStats.getSum(SavingsBudgetStat.BALANCE);
    if (balance == null) {
      clear(balanceLabel);
    }
    else {
      balanceLabel.setText((balance > 0 ? "+" : "") + format.format(balance));
      balanceLabel.setForeground(balance >= 0 ? normalColor : errorColor);
    }

    Double position = budgetStats.getLast().get(SavingsBudgetStat.END_OF_MONTH_POSITION);
    if (position == null) {
      clear(positionLabel);
    }
    else {
      positionLabel.setText(format.format(position));
      positionLabel.setForeground(normalColor);
    }
  }

  private String getShortDate(Integer monthId) {
    return BudgetSummaryView.getEstimatedPositionTitle(monthId);
  }

  private void clear(JLabel label) {
    label.setText("-");
    label.setForeground(normalColor);
  }

  public void selectionUpdated(GlobSelection selection) {
    update();
  }

  public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
    if (changeSet.containsChanges(SavingsBudgetStat.TYPE)) {
      update();
    }
  }

  public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
    if (changedTypes.contains(SavingsBudgetStat.TYPE)) {
      update();
    }
  }
}
