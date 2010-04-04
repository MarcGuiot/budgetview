package org.designup.picsou.gui.budget.summary;

import org.designup.picsou.gui.View;
import org.designup.picsou.gui.budget.BalanceDialog;
import org.designup.picsou.gui.budget.PositionDialog;
import org.designup.picsou.gui.budget.wizard.BudgetWizardDialog;
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
import static org.globsframework.model.utils.GlobMatchers.*;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.text.DecimalFormat;
import java.util.Set;
import java.util.SortedSet;

public class BudgetSummaryView extends View implements GlobSelectionListener, ChangeSetListener, ColorChangeListener {

  private JButton balanceLabel = new JButton();
  private JButton estimatedPositionLabel = new JButton();
  private JLabel estimatedPositionTitle = new JLabel();
  private JButton uncategorizedButton = new JButton();
  private JLabel multiSelectionLabel = new JLabel();
  private final DecimalFormat format = Formatting.DECIMAL_FORMAT;
  private JLabel helpMessage = new JLabel();

  private BudgetWizardDialog budgetWizardDialog;

  private AmountColors amountColors;
  private Color normalColor;
  private Color errorColor;
  private SplitsNode<JButton> openDetailsNode;

  public BudgetSummaryView(GlobRepository repository, Directory directory) {
    super(repository, directory);
    repository.addChangeListener(this);
    selectionService.addListener(this, Month.TYPE);
    colorService.addListener(this);

    this.amountColors = new AmountColors(directory);

    this.budgetWizardDialog = new BudgetWizardDialog(repository, directory);

    update();
  }

  public void colorsChanged(ColorLocator colorLocator) {
    normalColor = colorLocator.get("block.total");
    errorColor = colorLocator.get("block.total.error");
  }

  public void registerComponents(GlobsPanelBuilder parentBuilder) {
    GlobsPanelBuilder builder = new GlobsPanelBuilder(getClass(), "/layout/budget/budgetSummaryView.splits",
                                                      repository, directory);
    builder.add("balanceLabel", balanceLabel);
    builder.add("positionLabel", estimatedPositionLabel);
    builder.add("positionTitle", estimatedPositionTitle);
    builder.add("uncategorized", uncategorizedButton);
    builder.add("multiSelectionLabel", multiSelectionLabel);
    openDetailsNode = builder.add("openDetails", new JButton(new OpenDetailsAction(directory)));

    uncategorizedButton.addActionListener(new GotoUncategorizedAction());
    balanceLabel.addActionListener(new OpenBalanceAction());
    estimatedPositionLabel.addActionListener(new OpenPositionAction());
    builder.add("helpMessage", helpMessage);
    helpMessage.setVisible(false);

    parentBuilder.add("budgetSummaryView", builder);
  }

  private HyperlinkButtonUI createHyperlinkButtonUI() {
    HyperlinkButtonUI hyperlinkButtonUI = new HyperlinkButtonUI();
    hyperlinkButtonUI.setAutoHideEnabled(false);
    hyperlinkButtonUI.setUseNormalColorWhenDisabled(true);
    return hyperlinkButtonUI;
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
      clear(balanceLabel);
      clear(estimatedPositionLabel);
      clear(uncategorizedButton);
      updateHelpMessage();
      return;
    }

    Double balance = budgetStats.getSum(BudgetStat.MONTH_BALANCE);
    if (balance == null) {
      clear(balanceLabel);
    }
    else {
      balanceLabel.setText((balance > 0 ? "+" : "") + format.format(balance));
      balanceLabel.setForeground(balance >= 0 ? normalColor : errorColor);
    }

    updateEstimatedPosition(selectedMonthIds);

    Double uncategorized = budgetStats.getSum(BudgetStat.UNCATEGORIZED_ABS);
    if ((uncategorized != null) && (uncategorized > 0.01)) {
      uncategorizedButton.setText(format.format(uncategorized));
      uncategorizedButton.setForeground(errorColor);
      uncategorizedButton.setEnabled(true);
    }
    else {
      clear(uncategorizedButton);
      uncategorizedButton.setEnabled(false);
    }

    updateHelpMessage();
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
      estimatedPositionLabel.setToolTipText(Lang.get("budgetSummaryView.estimated.tooltip"));
    }
    else {
      estimatedPositionTitle.setText(Lang.get("budgetSummaryView.real.title"));
      estimatedPositionLabel.setToolTipText(null);
    }
  }

  public static String getEstimatedPositionTitle(Integer monthId) {
    return Lang.get("budgetSummaryView.estimated.title",
                    Month.getShortMonthLabelWithYear(monthId).toLowerCase());
  }

  private void setEstimatedPosition(Double amount) {
    if (amount == null) {
      clear(estimatedPositionLabel);
      return;
    }

    String text = Formatting.toString(amount);
    estimatedPositionLabel.setText(text);

    double diff = amount - AccountPositionThreshold.getValue(repository);
    estimatedPositionLabel.setForeground(amountColors.getTextColor(diff));
  }

  private Glob getBudgetStat(Integer lastSelectedMonthId) {
    return repository.find(Key.create(BudgetStat.TYPE, lastSelectedMonthId));
  }

  private Double getEndOfMonthPosition(Glob budgetStat) {
    return budgetStat.get(BudgetStat.END_OF_MONTH_ACCOUNT_POSITION);
  }

  private void clear(JLabel label) {
    label.setText("-");
    label.setForeground(normalColor);
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
        changeSet.containsChanges(UserPreferences.TYPE) ||
        changeSet.containsChanges(AccountPositionThreshold.TYPE)) {
      update();
    }
  }

  public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
    if (changedTypes.contains(BudgetStat.TYPE) ||
        changedTypes.contains(Series.TYPE) ||
        changedTypes.contains(UserPreferences.TYPE) ||
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
      PositionDialog dialog = new PositionDialog(repository, directory);
      dialog.show(selectionService.getSelection(Month.TYPE).getSortedSet(Month.ID));
    }
  }

  private class OpenDetailsAction extends AbstractAction {

    private GlobList selectedMonths;

    private OpenDetailsAction(Directory directory) {
      directory.get(SelectionService.class).addListener(new GlobSelectionListener() {
        public void selectionUpdated(GlobSelection selection) {
          selectedMonths = selection.getAll(Month.TYPE);
          selectedMonths.sort(Month.ID);

          setEnabled(!selectedMonths.isEmpty());
        }
      }, Month.TYPE);
    }

    public void actionPerformed(ActionEvent e) {
      repository.update(UserPreferences.KEY, UserPreferences.SHOW_BUDGET_VIEW_HELP_MESSAGE, false);
      budgetWizardDialog.show();
    }
  }

  private class GotoUncategorizedAction extends AbstractAction {
    public void actionPerformed(ActionEvent e) {
      directory.get(NavigationService.class).gotoUncategorized();
    }
  }

  private void updateHelpMessage() {
    Glob prefs = repository.find(UserPreferences.KEY);
    boolean showHelp =
      (prefs != null)
      && prefs.isTrue(UserPreferences.SHOW_BUDGET_VIEW_HELP_MESSAGE)
      && repository.contains(Series.TYPE, not(fieldEquals(Series.ID, Series.UNCATEGORIZED_SERIES_ID)));

    helpMessage.setVisible(showHelp);
    if (openDetailsNode != null) {
      openDetailsNode.applyStyle(showHelp ? "highlightedRoundButton" : "roundButton");
    }
  }
}

