package org.designup.picsou.gui.categorization.components;

import org.designup.picsou.gui.series.SeriesEditionDialog;
import org.designup.picsou.model.*;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.SelectionService;
import org.globsframework.gui.splits.repeat.RepeatCellBuilder;
import org.globsframework.gui.splits.repeat.RepeatComponentFactory;
import org.globsframework.gui.splits.utils.Disposable;
import org.globsframework.model.*;
import org.globsframework.model.format.DescriptionService;
import org.globsframework.model.format.GlobListStringifier;
import org.globsframework.model.format.GlobStringifier;
import org.globsframework.model.utils.DefaultChangeSetListener;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public abstract class AbstractSeriesComponentFactory implements RepeatComponentFactory<Glob> {
  protected JRadioButton invisibleSelector;
  protected ButtonGroup buttonGroup = new ButtonGroup();

  protected GlobListStringifier seriesStringifier;
  protected GlobStringifier subSeriesStringifier;
  protected GlobStringifier budgetAreaStringifier;

  protected SeriesEditionDialog seriesEditionDialog;
  protected GlobRepository repository;
  protected Directory directory;
  protected Window parent;
  protected SelectionService selectionService;

  protected GlobList currentTransactions = GlobList.EMPTY;

  public AbstractSeriesComponentFactory(JRadioButton invisibleSelector,
                                        SeriesEditionDialog seriesEditionDialog,
                                        GlobRepository repository,
                                        Directory directory) {
    this.invisibleSelector = invisibleSelector;
    this.buttonGroup.add(invisibleSelector);

    this.seriesEditionDialog = seriesEditionDialog;
    this.repository = repository;
    this.directory = directory;
    this.parent = directory.get(JFrame.class);

    DescriptionService descriptionService = directory.get(DescriptionService.class);
    seriesStringifier = descriptionService.getListStringifier(Series.TYPE);
    subSeriesStringifier = descriptionService.getStringifier(SubSeries.TYPE);
    budgetAreaStringifier = descriptionService.getStringifier(BudgetArea.TYPE);

    this.selectionService = directory.get(SelectionService.class);
    this.selectionService.addListener(new GlobSelectionListener() {
      public void selectionUpdated(GlobSelection selection) {
        currentTransactions = selection.getAll(Transaction.TYPE);
      }
    }, Transaction.TYPE);

  }

  protected JRadioButton createSeriesSelector(final String label,
                                              final Key seriesKey,
                                              final Key subSeriesKey) {
    return new JRadioButton(new AbstractAction(label) {
      public void actionPerformed(ActionEvent e) {
        try {
          repository.startChangeSet();
          for (Glob transaction : currentTransactions) {
            repository.setTarget(transaction.getKey(), Transaction.SERIES, seriesKey);
            repository.setTarget(transaction.getKey(), Transaction.SUB_SERIES, subSeriesKey);
          }
        }
        finally {
          repository.completeChangeSet();
        }
      }
    });
  }

  protected class EditSeriesAction extends AbstractAction {

    private Key seriesKey;

    protected EditSeriesAction(Key seriesKey) {
      this.seriesKey = seriesKey;
    }

    public void actionPerformed(ActionEvent e) {
      Glob series = repository.get(seriesKey);
      seriesEditionDialog.show(series, selectionService.getSelection(Month.TYPE).getValueSet(Month.ID));
    }
  }

}
