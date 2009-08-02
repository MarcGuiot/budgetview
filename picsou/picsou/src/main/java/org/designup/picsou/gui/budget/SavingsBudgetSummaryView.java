package org.designup.picsou.gui.budget;

import org.designup.picsou.gui.View;
import org.designup.picsou.gui.description.Formatting;
import org.designup.picsou.gui.model.SavingsBalanceStat;
import org.designup.picsou.model.Month;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.splits.color.ColorChangeListener;
import org.globsframework.gui.splits.color.ColorLocator;
import org.globsframework.model.ChangeSetListener;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.ChangeSet;
import org.globsframework.model.utils.GlobMatchers;
import org.globsframework.utils.directory.Directory;
import org.globsframework.metamodel.GlobType;

import javax.swing.*;
import java.awt.*;
import java.text.DecimalFormat;
import java.util.Set;

public class SavingsBudgetSummaryView extends View implements GlobSelectionListener, ChangeSetListener, ColorChangeListener {
  private JLabel balanceLabel = new JLabel();
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
    GlobsPanelBuilder builder = new GlobsPanelBuilder(getClass(), "/layout/savingsBudgetSummaryView.splits",
                                                      repository, directory);
    builder.add("balanceLabel", balanceLabel);
    builder.add("positionLabel", positionLabel);
    builder.add("multiSelectionLabel", multiSelectionLabel);

    parentBuilder.add("budgetSummaryView", builder);
  }

  public void update() {

    Set<Integer> selectedMonthIds = selectionService.getSelection(Month.TYPE).getValueSet(Month.ID);
    if (selectedMonthIds.size() > 1) {
      multiSelectionLabel.setText(Lang.get("budgetSummaryView.multimonth", selectedMonthIds.size()));
      multiSelectionLabel.setVisible(true);
    }
    else {
      multiSelectionLabel.setVisible(false);
    }

    GlobList balanceStats =
      repository.getAll(SavingsBalanceStat.TYPE, GlobMatchers.fieldIn(SavingsBalanceStat.MONTH, selectedMonthIds))
        .sort(SavingsBalanceStat.MONTH);

    if (balanceStats.size() == 0) {
      clear(balanceLabel);
      clear(positionLabel);
      return;
    }

    Double balance = balanceStats.getSum(SavingsBalanceStat.BALANCE);
    if (balance == null) {
      clear(balanceLabel);
    }
    else {
      balanceLabel.setText((balance > 0 ? "+" : "") + format.format(balance));
      balanceLabel.setForeground(balance >= 0 ? normalColor : errorColor);
    }

    Double position = balanceStats.getLast().get(SavingsBalanceStat.END_OF_MONTH_POSITION);
    if (position == null) {
      clear(positionLabel);
    }
    else {
      positionLabel.setText(format.format(position));
      positionLabel.setForeground(normalColor);
    }
  }

  private void clear(JLabel label) {
    label.setText("-");
    label.setForeground(normalColor);
  }

  public void selectionUpdated(GlobSelection selection) {
    update();
  }

  public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
    if (changeSet.containsChanges(SavingsBalanceStat.TYPE)) {
      update();
    }
  }

  public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
    if (changedTypes.contains(SavingsBalanceStat.TYPE)) {
      update();
    }
  }
}
