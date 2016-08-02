package com.budgetview.desktop.series;

import com.budgetview.desktop.View;
import com.budgetview.desktop.card.NavigationService;
import com.budgetview.desktop.description.Formatting;
import com.budgetview.desktop.model.BudgetStat;
import com.budgetview.model.Month;
import com.budgetview.model.Series;
import com.budgetview.model.Transaction;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.splits.color.ColorChangeListener;
import org.globsframework.gui.splits.color.ColorLocator;
import org.globsframework.gui.splits.utils.GuiUtils;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.ChangeSet;
import org.globsframework.model.ChangeSetListener;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.text.DecimalFormat;
import java.util.Set;
import java.util.SortedSet;

import static org.globsframework.model.utils.GlobMatchers.fieldIn;

public class UncategorizedSummaryView
  extends View
  implements GlobSelectionListener, ChangeSetListener, ColorChangeListener {

  private JPanel panel = new JPanel();
  private JButton uncategorizedButton = new JButton();

  private final DecimalFormat format = Formatting.DECIMAL_FORMAT;

  public UncategorizedSummaryView(GlobRepository repository, Directory directory) {
    super(repository, directory);
    repository.addChangeListener(this);
    selectionService.addListener(this, Month.TYPE);
    colorService.addListener(this);
    update();
  }

  public void colorsChanged(ColorLocator colorLocator) {
    uncategorizedButton.setForeground(colorLocator.get("budgetSummary.uncategorized"));
  }

  public void registerComponents(GlobsPanelBuilder parentBuilder) {
    GlobsPanelBuilder builder = new GlobsPanelBuilder(getClass(), "/layout/dashboard/uncategorizedSummaryView.splits",
                                                      repository, directory);
    builder.add("uncategorizedSummaryView", panel);
    builder.add("uncategorized", uncategorizedButton);
    uncategorizedButton.addActionListener(new GotoUncategorizedAction());
    parentBuilder.add("uncategorizedSummaryView", builder);
  }

  public void update() {

    SortedSet<Integer> selectedMonthIds = selectionService.getSelection(Month.TYPE).getSortedSet(Month.ID);
    GlobList budgetStats =
      repository.getAll(BudgetStat.TYPE, fieldIn(BudgetStat.MONTH, selectedMonthIds))
        .sort(BudgetStat.MONTH);

    if (!repository.contains(Transaction.TYPE) || budgetStats.isEmpty()) {
      hideUncategorized();
      return;
    }

    Double uncategorized = budgetStats.getSum(BudgetStat.UNCATEGORIZED_ABS);
    if ((uncategorized != null) && (uncategorized > 0.01)) {
      showUncategorized(uncategorized);
    }
    else {
      hideUncategorized();
    }
  }

  private void showUncategorized(Double uncategorized) {
    uncategorizedButton.setText(format.format(uncategorized));
    panel.setVisible(true);
    GuiUtils.revalidate(panel);
  }

  private void hideUncategorized() {
    panel.setVisible(false);
    GuiUtils.revalidate(panel);
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
