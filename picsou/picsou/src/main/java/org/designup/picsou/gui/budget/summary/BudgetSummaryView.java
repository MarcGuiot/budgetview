package org.designup.picsou.gui.budget.summary;

import org.designup.picsou.gui.View;
import org.designup.picsou.gui.accounts.chart.MainDailyPositionsChartView;
import org.designup.picsou.gui.accounts.position.AccountPositionLabels;
import org.designup.picsou.gui.card.NavigationService;
import com.budgetview.shared.gui.histochart.HistoChartConfig;
import org.designup.picsou.gui.description.Formatting;
import org.designup.picsou.gui.model.BudgetStat;
import org.designup.picsou.gui.series.analysis.histobuilders.range.ScrollableHistoChartRange;
import org.designup.picsou.model.Account;
import org.designup.picsou.model.Month;
import org.designup.picsou.model.Series;
import org.designup.picsou.model.Transaction;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.splits.SplitsNode;
import org.globsframework.gui.splits.color.ColorChangeListener;
import org.globsframework.gui.splits.color.ColorLocator;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.ChangeSet;
import org.globsframework.model.ChangeSetListener;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.text.DecimalFormat;
import java.util.Set;
import java.util.SortedSet;

import static org.globsframework.model.utils.GlobMatchers.fieldIn;

public class BudgetSummaryView
  extends View
  implements GlobSelectionListener, ChangeSetListener, ColorChangeListener {

  private JButton uncategorizedButton = new JButton();
  private SplitsNode<JButton> uncategorizedButtonNode;
  private JLabel multiSelectionLabel = new JLabel();

  private final DecimalFormat format = Formatting.DECIMAL_FORMAT;

  private Color normalColor;
  private Color errorColor;

  public BudgetSummaryView(GlobRepository repository, Directory directory) {
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
    GlobsPanelBuilder builder = new GlobsPanelBuilder(getClass(), "/layout/budget/budgetSummaryView.splits",
                                                      repository, directory);

    AccountPositionLabels.registerReferencePositionLabels(builder, Account.MAIN_SUMMARY_ACCOUNT_ID,
                                                          "lastPositionLabel", "lastPositionTitle",
                                                          "budgetSummaryView.position.title");

    uncategorizedButtonNode = builder.add("uncategorized", uncategorizedButton);
    builder.add("multiSelectionLabel", multiSelectionLabel);

    uncategorizedButton.addActionListener(new GotoUncategorizedAction());

    MainDailyPositionsChartView chartView =
      new MainDailyPositionsChartView(new ScrollableHistoChartRange(0, 1, true, repository),
                                      new HistoChartConfig(true, false, true, true, true, true, false, true, false, true),
                                      "chart", repository, directory, "daily.budgetSummary");
    chartView.installHighlighting();
    chartView.setShowFullMonthLabels(true);
    chartView.registerComponents(builder);

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
      repository.getAll(BudgetStat.TYPE, fieldIn(BudgetStat.MONTH, selectedMonthIds))
        .sort(BudgetStat.MONTH);

    if (!repository.contains(Transaction.TYPE) || budgetStats.isEmpty()) {
      clearUncategorized();
      return;
    }

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

  private void clear(JButton button) {
    button.setText("-");
    button.setForeground(normalColor);
  }

  public void selectionUpdated(GlobSelection selection) {
    update();
  }

  public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
    if (changeSet.containsChanges(BudgetStat.TYPE) ||
        changeSet.containsChanges(Series.TYPE)) {
      update();
    }
  }

  public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
    if (changedTypes.contains(BudgetStat.TYPE) ||
        changedTypes.contains(Series.TYPE)) {
      update();
    }
  }

  private class GotoUncategorizedAction extends AbstractAction {
    public void actionPerformed(ActionEvent e) {
      directory.get(NavigationService.class).gotoUncategorizedForSelectedMonths();
    }
  }
}

