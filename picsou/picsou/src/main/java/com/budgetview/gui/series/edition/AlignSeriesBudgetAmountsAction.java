package com.budgetview.gui.series.edition;

import com.budgetview.gui.description.Formatting;
import com.budgetview.model.BudgetArea;
import com.budgetview.model.Series;
import com.budgetview.model.SeriesBudget;
import com.budgetview.utils.Lang;
import org.globsframework.gui.actions.MultiSelectionAction;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.util.Set;

public class AlignSeriesBudgetAmountsAction extends MultiSelectionAction {
  private GlobList seriesBudgets = new GlobList();
  private JLabel label;

  public AlignSeriesBudgetAmountsAction(GlobRepository repository, Directory directory) {
    super(SeriesBudget.TYPE, repository, directory);
    getActualAmountLabel().setToolTipText(Lang.get("seriesAmountEdition.alignValue.actual.tooltip"));
  }

  protected String getLabel(GlobList selection) {
    return "";
  }

  public JLabel getActualAmountLabel() {
    if (label == null) {
      label = new JLabel();
    }
    return label;
  }

  protected void processSelection(GlobList selection) {
    seriesBudgets = selection.getAll(SeriesBudget.TYPE);
    setEnabled(!seriesBudgets.isEmpty());
    updateLabel();
  }

  private void updateLabel() {
    Set<Double> amounts = seriesBudgets.getValueSet(SeriesBudget.ACTUAL_AMOUNT);
    if (amounts.size() != 1) {
      getActualAmountLabel().setText(Lang.get("seriesAmountEdition.alignValue.actual"));
      return;
    }
    Double value = amounts.iterator().next();
    if (value == null) {
      value = 0.00;
    }
    getActualAmountLabel().setText(Formatting.toString(value, getBudgetArea()));
  }

  protected void processClick(GlobList selection, GlobRepository repository, Directory directory) {
    repository.startChangeSet();
    try {
      double lastValue = 0.00;
      for (Glob seriesBudget : seriesBudgets.sort(SeriesBudget.MONTH)) {
        Double newValue = seriesBudget.get(SeriesBudget.ACTUAL_AMOUNT);
        if (newValue == null) {
          newValue = lastValue;
        }
        repository.update(seriesBudget.getKey(), SeriesBudget.PLANNED_AMOUNT, newValue);
        lastValue = newValue;
      }
    }
    finally {
      repository.completeChangeSet();
    }
  }

  private BudgetArea getBudgetArea() {
    return Series.getBudgetArea(seriesBudgets.getFirst().get(SeriesBudget.SERIES), repository);
  }
}
