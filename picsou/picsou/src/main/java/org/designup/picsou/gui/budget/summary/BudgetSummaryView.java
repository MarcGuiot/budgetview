package org.designup.picsou.gui.budget.summary;

import org.designup.picsou.gui.View;
import org.designup.picsou.gui.signpost.guides.EndOfMonthPositionSignpost;
import org.designup.picsou.gui.signpost.Signpost;
import org.designup.picsou.gui.budget.BalanceDialog;
import org.designup.picsou.gui.budget.PositionDialog;
import org.designup.picsou.gui.card.NavigationService;
import org.designup.picsou.gui.description.Formatting;
import org.designup.picsou.gui.model.BudgetStat;
import org.designup.picsou.gui.utils.AmountColors;
import org.designup.picsou.model.*;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.SelectionService;
import org.globsframework.gui.splits.SplitsNode;
import org.globsframework.gui.splits.color.ColorChangeListener;
import org.globsframework.gui.splits.color.ColorLocator;
import org.globsframework.gui.splits.components.HyperlinkButtonUI;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.*;
import org.globsframework.model.utils.GlobMatchers;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.text.DecimalFormat;
import java.util.Set;
import java.util.SortedSet;

public class BudgetSummaryView extends View implements GlobSelectionListener, ChangeSetListener, ColorChangeListener {

  private JButton balanceButton = new JButton();
  private JButton estimatedPositionButton = new JButton();
  private JLabel estimatedPositionTitle = new JLabel();
  private JButton uncategorizedButton = new JButton();
  private SplitsNode<JButton> uncategorizedButtonNode;
  private JLabel multiSelectionLabel = new JLabel();

  private final DecimalFormat format = Formatting.DECIMAL_FORMAT;

  private AmountColors amountColors;
  private Color normalColor;
  private Color errorColor;

  public BudgetSummaryView(GlobRepository repository, Directory directory) {
    super(repository, directory);
    repository.addChangeListener(this);
    selectionService.addListener(this, Month.TYPE);
    colorService.addListener(this);

    this.amountColors = new AmountColors(directory);

    update();
  }

  public void colorsChanged(ColorLocator colorLocator) {
    normalColor = colorLocator.get("block.total");
    errorColor = colorLocator.get("block.total.error");
  }

  public void registerComponents(GlobsPanelBuilder parentBuilder) {
    GlobsPanelBuilder builder = new GlobsPanelBuilder(getClass(), "/layout/budget/budgetSummaryView.splits",
                                                      repository, directory);

    builder.add("balanceLabel", balanceButton);
    builder.add("positionLabel", estimatedPositionButton);
    builder.add("positionTitle", estimatedPositionTitle);
    uncategorizedButtonNode = builder.add("uncategorized", uncategorizedButton);
    builder.add("multiSelectionLabel", multiSelectionLabel);

    uncategorizedButton.addActionListener(new GotoUncategorizedAction());
    balanceButton.addActionListener(new OpenBalanceAction());
    estimatedPositionButton.addActionListener(new OpenPositionAction());
    Signpost signpost = new EndOfMonthPositionSignpost(repository, directory);
    signpost.attach(estimatedPositionButton);

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

    GlobList budgetStats =
      repository.getAll(BudgetStat.TYPE, GlobMatchers.fieldIn(BudgetStat.MONTH, selectedMonthIds))
        .sort(BudgetStat.MONTH);

    if (!repository.contains(Transaction.TYPE) || budgetStats.isEmpty()) {
      clear(balanceButton);
      clear(estimatedPositionButton);
      clearUncategorized();
      return;
    }

    Double balance = budgetStats.getSum(BudgetStat.MONTH_BALANCE);
    if (balance == null) {
      clear(balanceButton);
    }
    else {
      balanceButton.setText((balance > 0 ? "+" : "") + format.format(balance));
      balanceButton.setForeground(balance >= 0 ? normalColor : errorColor);
    }

    updateEstimatedPosition(selectedMonthIds);

    Double uncategorized = budgetStats.getSum(BudgetStat.UNCATEGORIZED_ABS);
    if ((uncategorized != null) && (uncategorized > 0.01)) {
      setUncategorized(uncategorized);
    }
    else {
      clearUncategorized();
    }
  }

  private void setUncategorized(Double uncategorized) {
    uncategorizedButton.setText(format.format(uncategorized));
    uncategorizedButton.setForeground(errorColor);
    uncategorizedButton.setEnabled(true);
    if (uncategorizedButtonNode != null) {
      uncategorizedButtonNode.applyStyle("uncategorizedEnabled");
    }
  }

  private void clearUncategorized() {
    clear(uncategorizedButton);
    uncategorizedButton.setEnabled(false);
    if (uncategorizedButtonNode != null) {
      uncategorizedButtonNode.applyStyle("uncategorizedDisabled");
    }
  }

  private void updateEstimatedPosition(SortedSet<Integer> selectedMonthIds) {
    if (selectedMonthIds.isEmpty()) {
      setEstimatedPosition(null);
      return;
    }
    Integer lastSelectedMonthId = selectedMonthIds.last();

    Glob budgetStat = getBudgetStat(lastSelectedMonthId);
    if (budgetStat == null) {
      setEstimatedPosition(null);
      return;
    }

    String lastDay = Formatting.toString(Month.getLastDay(lastSelectedMonthId));
    String dateLabel = Lang.get("accountView.total.date", lastDay);

    Double amount = getEndOfMonthPosition(budgetStat);
    setEstimatedPosition(amount);

    Integer lastImportDate = repository.get(CurrentMonth.KEY).get(CurrentMonth.LAST_TRANSACTION_MONTH);
    if (lastSelectedMonthId >= lastImportDate) {
      estimatedPositionTitle.setText(getEstimatedPositionTitle(lastSelectedMonthId));
      estimatedPositionButton.setToolTipText(Lang.get("budgetSummaryView.estimated.tooltip"));
    }
    else {
      estimatedPositionTitle.setText(Lang.get("budgetSummaryView.real.title"));
      estimatedPositionButton.setToolTipText(null);
    }
  }

  public static String getEstimatedPositionTitle(Integer monthId) {
    return Lang.get("budgetSummaryView.estimated.title",
                    Month.getShortMonthLabelWithYear(monthId).toLowerCase());
  }

  private void setEstimatedPosition(Double amount) {
    if (amount == null) {
      clear(estimatedPositionButton);
      return;
    }

    String text = Formatting.toString(amount);
    estimatedPositionButton.setText(text);

    double diff = amount - AccountPositionThreshold.getValue(repository);
    estimatedPositionButton.setForeground(amountColors.getTextColor(diff));
  }

  private Glob getBudgetStat(Integer lastSelectedMonthId) {
    return repository.find(Key.create(BudgetStat.TYPE, lastSelectedMonthId));
  }

  private Double getEndOfMonthPosition(Glob budgetStat) {
    return budgetStat.get(BudgetStat.END_OF_MONTH_ACCOUNT_POSITION);
  }

  private void clear(JButton button) {
    button.setText("-");
    button.setForeground(normalColor);
  }

  public void selectionUpdated(GlobSelection selection) {
    update();
  }

  public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
    if (changeSet.containsChanges(BudgetStat.TYPE) ||
        changeSet.containsChanges(Series.TYPE) ||
        changeSet.containsChanges(AccountPositionThreshold.TYPE)) {
      update();
    }
  }

  public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
    if (changedTypes.contains(BudgetStat.TYPE) ||
        changedTypes.contains(Series.TYPE) ||
        changedTypes.contains(AccountPositionThreshold.TYPE)) {
      update();
    }
  }

  private class OpenBalanceAction extends AbstractAction {

    public void actionPerformed(ActionEvent e) {
      BalanceDialog dialog = new BalanceDialog(repository, directory);
      dialog.show(selectionService.getSelection(Month.TYPE).getSortedSet(Month.ID));
    }
  }

  private class OpenPositionAction extends AbstractAction {

    public void actionPerformed(ActionEvent e) {

      SignpostStatus.setCompleted(SignpostStatus.END_OF_MONTH_POSITION_SHOWN, repository);

      PositionDialog dialog = new PositionDialog(repository, directory);
      dialog.show(selectionService.getSelection(Month.TYPE).getSortedSet(Month.ID));
    }
  }

  private class GotoUncategorizedAction extends AbstractAction {
    public void actionPerformed(ActionEvent e) {
      directory.get(NavigationService.class).gotoUncategorizedForSelectedMonths();
    }
  }
}

