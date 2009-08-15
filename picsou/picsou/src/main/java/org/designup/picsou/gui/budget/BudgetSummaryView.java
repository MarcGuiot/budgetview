package org.designup.picsou.gui.budget;

import org.designup.picsou.gui.View;
import org.designup.picsou.gui.components.JRoundedButton;
import org.designup.picsou.gui.accounts.BudgetSummaryDetailsDialog;
import org.designup.picsou.gui.description.Formatting;
import org.designup.picsou.gui.model.BalanceStat;
import org.designup.picsou.gui.utils.AmountColors;
import org.designup.picsou.model.AccountPositionThreshold;
import org.designup.picsou.model.CurrentMonth;
import org.designup.picsou.model.Month;
import org.designup.picsou.model.Transaction;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.SelectionService;
import org.globsframework.gui.splits.color.ColorChangeListener;
import org.globsframework.gui.splits.color.ColorLocator;
import org.globsframework.gui.splits.components.HyperlinkButtonUI;
import org.globsframework.gui.splits.ImageLocator;
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

  private JLabel balanceLabel = new JLabel();
  private JLabel estimatedPositionLabel = new JLabel();
  private JLabel estimatedPositionTitle = new JLabel();
  private JLabel uncategorizedLabel = new JLabel();
  private JLabel multiSelectionLabel = new JLabel();
  private final DecimalFormat format = Formatting.DECIMAL_FORMAT;

  private BudgetSummaryDetailsDialog budgetSummaryDetailsDialog;

  private AmountColors amountColors;
  private Color normalColor;
  private Color errorColor;

  public BudgetSummaryView(GlobRepository repository, Directory directory) {
    super(repository, directory);
    repository.addChangeListener(this);
    selectionService.addListener(this, Month.TYPE);
    colorService.addListener(this);

    this.amountColors = new AmountColors(directory);

    this.budgetSummaryDetailsDialog = new BudgetSummaryDetailsDialog(repository, directory);

    update();
  }

  public void colorsChanged(ColorLocator colorLocator) {
    normalColor = colorLocator.get("block.total");
    errorColor = colorLocator.get("block.total.error");
  }

  public void registerComponents(GlobsPanelBuilder parentBuilder) {
    GlobsPanelBuilder builder = new GlobsPanelBuilder(getClass(), "/layout/budgetSummaryView.splits",
                                                      repository, directory);
    builder.add("balanceLabel", balanceLabel);
    builder.add("positionLabel", estimatedPositionLabel);
    builder.add("positionTitle", estimatedPositionTitle);
    builder.add("uncategorizedLabel", uncategorizedLabel);
    builder.add("multiSelectionLabel", multiSelectionLabel);
    builder.add("openDetailsButton", createOpenDetailsButton());

    parentBuilder.add("budgetSummaryView", builder);
  }

  private JButton createOpenDetailsButton() {
    JRoundedButton button = JRoundedButton.createCircle(new OpenDetailsAction(directory), colorService);
    ImageLocator imageLocator = directory.get(ImageLocator.class);
    button.setIcon(imageLocator.get("button_magnifier.png"));
    button.setPressedIcon(imageLocator.get("button_magnifier.png"));
    button.setDisabledIcon(imageLocator.get("button_magnifier_disabled.png"));
    return button;
  }

  private HyperlinkButtonUI createHyperlinkButtonUI() {
    HyperlinkButtonUI hyperlinkButtonUI = new HyperlinkButtonUI();
    hyperlinkButtonUI.setAutoHideIfDisabled(false);
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

    GlobList balanceStats =
      repository.getAll(BalanceStat.TYPE, GlobMatchers.fieldIn(BalanceStat.MONTH, selectedMonthIds))
        .sort(BalanceStat.MONTH);

    if (!repository.contains(Transaction.TYPE) || balanceStats.isEmpty()) {
      clear(balanceLabel);
      clear(estimatedPositionLabel);
      clear(uncategorizedLabel);
      return;
    }

    Double balance = balanceStats.getSum(BalanceStat.MONTH_BALANCE);
    if (balance == null) {
      clear(balanceLabel);
    }
    else {
      balanceLabel.setText((balance > 0 ? "+" : "") + format.format(balance));
      balanceLabel.setForeground(balance >= 0 ? normalColor : errorColor);
    }

    updateEstimatedPosition(selectedMonthIds);

    Double uncategorized = balanceStats.getSum(BalanceStat.UNCATEGORIZED);
    if ((uncategorized != null) && (uncategorized != 0)) {
      uncategorizedLabel.setText(format.format(uncategorized));
      uncategorizedLabel.setForeground(errorColor);
    }
    else {
      clear(uncategorizedLabel);
    }

  }

  private void updateEstimatedPosition(SortedSet<Integer> selectedMonthIds) {
    if (selectedMonthIds.isEmpty()) {
      setEstimatedPosition(null);
      return;
    }
    Integer lastSelectedMonthId = selectedMonthIds.last();

    Glob balanceStat = getBalanceStat(lastSelectedMonthId);
    if (balanceStat == null) {
      setEstimatedPosition(null);
      return;
    }

    String lastDay = Formatting.toString(Month.getLastDay(lastSelectedMonthId));
    String dateLabel = Lang.get("accountView.total.date", lastDay);

    Double amount = getEndOfMonthPosition(balanceStat);
    setEstimatedPosition(amount);

    Integer lastImportDate = repository.get(CurrentMonth.KEY).get(CurrentMonth.LAST_TRANSACTION_MONTH);
    if (lastSelectedMonthId >= lastImportDate) {
      estimatedPositionTitle.setText(Lang.get("accountView.estimated.title"));
      estimatedPositionLabel.setToolTipText(Lang.get("accountView.estimated.tooltip"));
    }
    else {
      estimatedPositionTitle.setText(Lang.get("accountView.real.title"));
      estimatedPositionLabel.setToolTipText(null);
    }
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

  private Glob getBalanceStat(Integer lastSelectedMonthId) {
    return repository.find(Key.create(BalanceStat.TYPE, lastSelectedMonthId));
  }

  private Double getEndOfMonthPosition(Glob balanceStat) {
    return balanceStat.get(BalanceStat.END_OF_MONTH_ACCOUNT_POSITION);
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
    if (changeSet.containsChanges(BalanceStat.TYPE) ||
        changeSet.containsChanges(AccountPositionThreshold.TYPE)) {
      update();
    }
  }

  public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
    if (changedTypes.contains(BalanceStat.TYPE)) {
      update();
    }
  }

  private class OpenDetailsAction extends AbstractAction {

    private GlobList selectedMonths;

    private OpenDetailsAction(Directory directory) {
      directory.get(SelectionService.class).addListener(new GlobSelectionListener() {
        public void selectionUpdated(GlobSelection selection) {
          selectedMonths = selection.getAll(Month.TYPE);
          selectedMonths.sort(Month.ID);

          if (selectedMonths.isEmpty()) {
            setEnabled(false);
            return;
          }

          Integer monthId = selectedMonths.getLast().get(Month.ID);
          Integer currentMonthId = CurrentMonth.getLastTransactionMonth(repository);
          boolean enabled = monthId >= currentMonthId;
          setEnabled(enabled);
        }
      }, Month.TYPE);
    }

    public void actionPerformed(ActionEvent e) {
      budgetSummaryDetailsDialog.show(selectedMonths);
    }
  }

}
