package org.designup.picsou.gui.categorization.components;

import org.globsframework.model.format.GlobStringifier;
import org.globsframework.model.format.DescriptionService;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.GlobList;
import org.globsframework.model.Key;
import org.globsframework.gui.splits.repeat.RepeatComponentFactory;
import org.globsframework.gui.splits.repeat.RepeatCellBuilder;
import org.globsframework.gui.SelectionService;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.GlobSelection;
import org.globsframework.utils.directory.Directory;
import org.designup.picsou.model.Transaction;
import org.designup.picsou.model.Series;
import org.designup.picsou.model.Category;
import org.designup.picsou.model.BudgetArea;

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
  protected SelectionService selectionService;

  protected Glob currentTransaction;

  public AbstractSeriesComponentFactory(JToggleButton invisibleToggle, GlobRepository repository, Directory directory) {
    this.invisibleToggle = invisibleToggle;
    this.repository = repository;
    this.directory = directory;

    DescriptionService descriptionService = directory.get(DescriptionService.class);
    seriesStringifier = descriptionService.getStringifier(Series.TYPE);
    categoryStringifier = descriptionService.getStringifier(Category.TYPE);
    budgetAreaStringifier = descriptionService.getStringifier(BudgetArea.TYPE);

    this.selectionService = directory.get(SelectionService.class);
    this.selectionService.addListener(new GlobSelectionListener() {
      public void selectionUpdated(GlobSelection selection) {
          final GlobList selectedTransactions = selection.getAll(Transaction.TYPE);
          currentTransaction = selectedTransactions.size() == 1 ? selectedTransactions.get(0) : null;
      }
    }, Transaction.TYPE);

    this.buttonGroup.add(invisibleToggle);
  }

  protected JToggleButton createCategoryToggle(final String toggleLabel, final Key seriesKey, final Key categoryKey) {
    return new JToggleButton(new AbstractAction(toggleLabel) {
      public void actionPerformed(ActionEvent e) {
        repository.setTarget(currentTransaction.getKey(), Transaction.SERIES, seriesKey);
        repository.setTarget(currentTransaction.getKey(), Transaction.CATEGORY, categoryKey);
      }
    });
  }

  protected void createUpdatableCategoryToggle(final Glob category, final Key seriesKey,
                                               String repeatToggleName, BudgetArea budgetArea,
                                               RepeatCellBuilder cellBuilder, String toggleName) {

    String toggleLabel = categoryStringifier.toString(category, repository);
    final JToggleButton toggle = new JToggleButton(new AbstractAction(toggleLabel) {
      public void actionPerformed(ActionEvent e) {
        repository.setTarget(currentTransaction.getKey(), Transaction.SERIES, seriesKey);
        repository.setTarget(currentTransaction.getKey(), Transaction.CATEGORY, category.getKey());
      }
    });
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

}
