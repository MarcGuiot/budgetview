package org.designup.picsou.gui.series.edition;

import org.designup.picsou.model.SeriesBudget;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.SelectionService;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class AlignSeriesBudgetAmountsAction extends AbstractAction implements GlobSelectionListener {
  private GlobRepository repository;
  private GlobList seriesBudgets;

  public AlignSeriesBudgetAmountsAction(GlobRepository repository, Directory directory) {
    this.repository = repository;
    directory.get(SelectionService.class).addListener(this, SeriesBudget.TYPE);
    setEnabled(false);
  }

  public void selectionUpdated(GlobSelection selection) {
    seriesBudgets = selection.getAll(SeriesBudget.TYPE);
    setEnabled(!seriesBudgets.isEmpty());
  }

  public void actionPerformed(ActionEvent e) {
    repository.startChangeSet();
    try {
      for (Glob seriesBudget : seriesBudgets) {
        Double newValue = seriesBudget.get(SeriesBudget.OBSERVED_AMOUNT);
        if (newValue == null) {
          newValue = 0.00;
        }
        repository.update(seriesBudget.getKey(), SeriesBudget.AMOUNT, newValue);
      }
    }
    finally {
      repository.completeChangeSet();
    }
  }
}
