package org.designup.picsou.gui.categorization.components;

import org.designup.picsou.model.*;
import org.designup.picsou.gui.series.SeriesEditionDialog;
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
  protected JToggleButton invisibleToggle;
  protected ButtonGroup buttonGroup = new ButtonGroup();

  protected GlobListStringifier seriesStringifier;
  protected GlobStringifier categoryStringifier;
  protected GlobStringifier budgetAreaStringifier;

  protected SeriesEditionDialog seriesEditionDialog;
  protected GlobRepository repository;
  protected Directory directory;
  protected Window parent;
  protected SelectionService selectionService;

  protected GlobList currentTransactions = GlobList.EMPTY;

  public AbstractSeriesComponentFactory(JToggleButton invisibleToggle,
                                        SeriesEditionDialog seriesEditionDialog,
                                        GlobRepository repository,
                                        Directory directory) {
    this.invisibleToggle = invisibleToggle;
    this.seriesEditionDialog = seriesEditionDialog;
    this.repository = repository;
    this.directory = directory;
    this.parent = directory.get(JFrame.class);

    DescriptionService descriptionService = directory.get(DescriptionService.class);
    seriesStringifier = descriptionService.getListStringifier(Series.TYPE);
    categoryStringifier = descriptionService.getStringifier(Category.TYPE);
    budgetAreaStringifier = descriptionService.getStringifier(BudgetArea.TYPE);

    this.selectionService = directory.get(SelectionService.class);
    this.selectionService.addListener(new GlobSelectionListener() {
      public void selectionUpdated(GlobSelection selection) {
        currentTransactions = selection.getAll(Transaction.TYPE);
      }
    }, Transaction.TYPE);

    this.buttonGroup.add(invisibleToggle);
  }

  protected void createUpdatableCategoryToggle(final Glob category, final Key seriesKey,
                                               String repeatToggleName, BudgetArea budgetArea,
                                               RepeatCellBuilder cellBuilder, String toggleName) {
    String toggleLabel = categoryStringifier.toString(category, repository);
    final JToggleButton toggle = createSeriesToggle(toggleLabel, seriesKey, category.getKey());
    final Key key = category.getKey();
    final DefaultChangeSetListener categoryUpdateListener = new DefaultChangeSetListener() {
      public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
        if (changeSet.containsChanges(key)) {
          Glob category = repository.find(key);
          if (category != null) {
            toggle.setText(categoryStringifier.toString(category, repository));
          }
        }
      }
    };
    repository.addChangeListener(categoryUpdateListener);
    toggle.setName(toggleName);
    buttonGroup.add(toggle);
    cellBuilder.add(repeatToggleName, toggle);

    final CategoryUpdater updater =
      new CategoryUpdater(toggle, invisibleToggle, seriesKey, category.getKey(), budgetArea, repository, selectionService);
    cellBuilder.addDisposeListener(new Disposable() {
      public void dispose() {
        updater.dispose();
        buttonGroup.remove(toggle);
        repository.removeChangeListener(categoryUpdateListener);
      }
    });
  }

  protected JToggleButton createSeriesToggle(final String toggleLabel,
                                             final Key seriesKey,
                                             final Key categoryKey) {
    return new JToggleButton(new AbstractAction(toggleLabel) {
      public void actionPerformed(ActionEvent e) {
        try {
          repository.enterBulkDispatchingMode();
          for (Glob transaction : currentTransactions) {
            repository.setTarget(transaction.getKey(), Transaction.SERIES, seriesKey);
            repository.setTarget(transaction.getKey(), Transaction.CATEGORY, categoryKey);
          }
        }
        finally {
          repository.completeBulkDispatchingMode();
        }
      }
    });
  }

  protected class EditSeriesAction extends AbstractAction  {

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
