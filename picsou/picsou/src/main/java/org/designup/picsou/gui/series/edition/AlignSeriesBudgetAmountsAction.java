package org.designup.picsou.gui.series.edition;

import org.designup.picsou.gui.description.Formatting;
import org.designup.picsou.model.BudgetArea;
import org.designup.picsou.model.Series;
import org.designup.picsou.model.SeriesBudget;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.SelectionService;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.Set;

public class AlignSeriesBudgetAmountsAction extends AbstractAction implements GlobSelectionListener {
  private GlobRepository repository;
  private Directory directory;
  private GlobList seriesBudgets;
  private JLabel label = new JLabel();

  public AlignSeriesBudgetAmountsAction(GlobRepository repository, Directory directory) {
    this.repository = repository;
    this.directory = directory;
    directory.get(SelectionService.class).addListener(this, SeriesBudget.TYPE);
    setEnabled(false);
    label.setToolTipText(Lang.get("seriesAmountEdition.alignValue.actual.tooltip"));
  }

  public JLabel getActualAmountLabel() {
    return label;
  }

  public void selectionUpdated(GlobSelection selection) {
    seriesBudgets = selection.getAll(SeriesBudget.TYPE);
    setEnabled(!seriesBudgets.isEmpty());
    updateLabel();
  }

  private void updateLabel() {
    Set<Double> amounts = seriesBudgets.getValueSet(SeriesBudget.OBSERVED_AMOUNT);
    if (amounts.size() != 1) {
      label.setText(Lang.get("seriesAmountEdition.alignValue.actual"));
      return;
    }
    Double value = amounts.iterator().next();
    if (value == null) {
      value = 0.00;
    }
    label.setText(Formatting.toString(value, getBudgetArea()));
  }

  public void actionPerformed(ActionEvent e) {
    repository.startChangeSet();
    try {
      double lastValue = 0.00;
      for (Glob seriesBudget : seriesBudgets.sort(SeriesBudget.MONTH)) {
        Double newValue = seriesBudget.get(SeriesBudget.OBSERVED_AMOUNT);
        if (newValue == null) {
          newValue = lastValue;
        }
        repository.update(seriesBudget.getKey(), SeriesBudget.AMOUNT, newValue);
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
