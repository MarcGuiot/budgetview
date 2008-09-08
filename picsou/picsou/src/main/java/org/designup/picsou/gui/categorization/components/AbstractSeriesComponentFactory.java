package org.designup.picsou.gui.categorization.components;

import org.designup.picsou.gui.components.PicsouDialog;
import org.designup.picsou.model.BudgetArea;
import org.designup.picsou.model.Category;
import org.designup.picsou.model.Series;
import org.designup.picsou.model.Transaction;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.SelectionService;
import org.globsframework.gui.splits.repeat.RepeatCellBuilder;
import org.globsframework.gui.splits.repeat.RepeatComponentFactory;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.model.format.DescriptionService;
import org.globsframework.model.format.GlobStringifier;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;

public abstract class AbstractSeriesComponentFactory implements RepeatComponentFactory<Glob> {
  protected JToggleButton invisibleToggle;
  protected ButtonGroup buttonGroup = new ButtonGroup();

  protected GlobStringifier seriesStringifier;
  protected GlobStringifier categoryStringifier;
  protected GlobStringifier budgetAreaStringifier;

  protected GlobRepository repository;
  protected Directory directory;
  protected PicsouDialog dialog;
  protected SelectionService selectionService;

  protected GlobList currentTransactions = GlobList.EMPTY;

  public AbstractSeriesComponentFactory(JToggleButton invisibleToggle, GlobRepository repository, Directory directory, PicsouDialog dialog) {
    this.invisibleToggle = invisibleToggle;
    this.repository = repository;
    this.directory = directory;
    this.dialog = dialog;

    DescriptionService descriptionService = directory.get(DescriptionService.class);
    seriesStringifier = descriptionService.getStringifier(Series.TYPE);
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
                                               RepeatCellBuilder cellBuilder, String toggleName, PicsouDialog dialog) {

    String toggleLabel = categoryStringifier.toString(category, repository);
    final JToggleButton toggle = createSeriesToggle(toggleLabel, seriesKey, category.getKey(), dialog);
    toggle.setName(toggleName);
    cellBuilder.add(repeatToggleName, toggle);
    buttonGroup.add(toggle);

    final CategoryUpdater updater =
      new CategoryUpdater(toggle, invisibleToggle, seriesKey, category.getKey(), budgetArea, repository, selectionService);
    cellBuilder.addDisposeListener(new RepeatCellBuilder.DisposeListener() {
      public void dispose() {
        updater.dispose();
        buttonGroup.remove(toggle);
      }
    });
  }

  protected JToggleButton createSeriesToggle(final String toggleLabel,
                                             final Key seriesKey, final Key categoryKey,
                                             final PicsouDialog dialog) {
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

}
